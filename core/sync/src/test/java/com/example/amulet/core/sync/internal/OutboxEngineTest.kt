package com.example.amulet.core.sync.internal

import com.example.amulet.core.database.entity.OutboxActionEntity
import com.example.amulet.core.database.entity.OutboxActionStatus
import com.example.amulet.core.database.entity.OutboxActionType
import com.example.amulet.core.sync.processing.ActionProcessor
import com.example.amulet.core.sync.processing.ActionProcessorFactory
import com.example.amulet.shared.core.AppError
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class OutboxEngineTest {

    private val timeProvider = MutableTimeProvider(0L)
    private val config = OutboxSyncConfig(
        maxActionsPerSync = 10,
        inFlightTimeoutMillis = 5_000L,
        baseBackoffMillis = 1_000L,
        maxBackoffMillis = 8_000L
    )
    private val backoffPolicy = BackoffPolicy(
        baseDelayMillis = config.baseBackoffMillis,
        maxDelayMillis = config.maxBackoffMillis,
        jitterRatioMin = 1.0,
        jitterRatioMax = 1.0
    )
    private val resolver = ActionErrorResolver(backoffPolicy)

    @Test
    fun `successful processing marks action completed`() = runEngineTest(
        processorFactory = singleProcessorFactory { Ok(Unit) }
    ) { store, processedCount ->
        assertEquals(1, processedCount)
        val action = store.actions[DEFAULT_ID]!!
        assertEquals(OutboxActionStatus.COMPLETED, action.status)
        assertNull(action.lastError)
    }

    @Test
    fun `network errors schedule retry with backoff`() = runEngineTest(
        processorFactory = singleProcessorFactory { Err(AppError.Network) }
    ) { store, _ ->
        val action = store.actions[DEFAULT_ID]!!
        assertEquals(OutboxActionStatus.PENDING, action.status)
        assertEquals(1, action.retryCount)
        assertEquals(1_000L, action.availableAt)
        assertIs<ActionResolution.Retry>(resolver.resolve(action.copy(retryCount = 0), AppError.Network))
    }

    @Test
    fun `non retriable errors mark action failed`() = runEngineTest(
        processorFactory = singleProcessorFactory { Err(AppError.Validation(mapOf("field" to "error"))) }
    ) { store, _ ->
        val action = store.actions[DEFAULT_ID]!!
        assertEquals(OutboxActionStatus.FAILED, action.status)
        assertEquals(1, action.retryCount)
        assertEquals("Validation[field=error]", action.lastError)
    }

    @Test
    fun `missing processor marks action failed`() = runTest {
        val store = FakeOutboxActionStore(listOf(createAction()))
        val engine = OutboxEngine(
            store = store,
            processorFactory = object : ActionProcessorFactory {
                override fun get(type: OutboxActionType): ActionProcessor =
                    throw IllegalStateException("missing")
            },
            errorResolver = resolver,
            timeProvider = timeProvider,
            config = config
        )

        engine.run()

        val action = store.actions[DEFAULT_ID]!!
        assertEquals(OutboxActionStatus.FAILED, action.status)
        assertEquals(1, action.retryCount)
    }

    @Test
    fun `processor crash marks action failed`() = runEngineTest(
        processorFactory = singleProcessorFactory { throw IllegalArgumentException("boom") }
    ) { store, _ ->
        val action = store.actions[DEFAULT_ID]!!
        assertEquals(OutboxActionStatus.FAILED, action.status)
        assertEquals(1, action.retryCount)
        assertEquals("boom", action.lastError)
    }

    private fun runEngineTest(
        processorFactory: ActionProcessorFactory,
        assertions: (FakeOutboxActionStore, Int) -> Unit
    ) = runTest {
        val store = FakeOutboxActionStore(listOf(createAction()))
        val engine = OutboxEngine(store, processorFactory, resolver, timeProvider, config)

        val processed = engine.run()

        assertions(store, processed)
    }

    private fun singleProcessorFactory(block: suspend (OutboxActionEntity) -> com.github.michaelbull.result.Result<Unit, AppError>): ActionProcessorFactory =
        object : ActionProcessorFactory {
            override fun get(type: OutboxActionType): ActionProcessor = ActionProcessor { action ->
                block(action)
            }
        }

    private fun createAction(
        id: String = DEFAULT_ID
    ): OutboxActionEntity = OutboxActionEntity(
        id = id,
        type = OutboxActionType.HUG_SEND,
        payloadJson = "{}",
        status = OutboxActionStatus.PENDING,
        retryCount = 0,
        lastError = null,
        idempotencyKey = null,
        createdAt = 0L,
        updatedAt = 0L,
        availableAt = 0L,
        priority = 0,
        targetEntityId = null
    )

    private class FakeOutboxActionStore(actions: List<OutboxActionEntity>) : OutboxActionStore {
        val actions: MutableMap<String, OutboxActionEntity> = actions.associateBy { it.id }.toMutableMap()

        override suspend fun takeNextAction(now: Long): OutboxActionEntity? {
            val due = actions.values
                .filter { (it.status == OutboxActionStatus.PENDING || it.status == OutboxActionStatus.FAILED) && it.availableAt <= now }
                .sortedWith(compareByDescending<OutboxActionEntity> { it.priority }
                    .thenBy { it.availableAt }
                    .thenBy { it.createdAt })
                .firstOrNull() ?: return null

            val updated = due.copy(
                status = OutboxActionStatus.IN_FLIGHT,
                lastError = null,
                updatedAt = now,
                availableAt = now
            )
            actions[due.id] = updated
            return updated
        }

        override suspend fun markCompleted(action: OutboxActionEntity, completedAt: Long) {
            actions[action.id] = action.copy(
                status = OutboxActionStatus.COMPLETED,
                lastError = null,
                updatedAt = completedAt,
                availableAt = completedAt
            )
        }

        override suspend fun markForRetry(action: OutboxActionEntity, delayMillis: Long, errorMessage: String?, updatedAt: Long) {
            actions[action.id] = action.copy(
                status = OutboxActionStatus.PENDING,
                retryCount = action.retryCount + 1,
                lastError = errorMessage,
                updatedAt = updatedAt,
                availableAt = updatedAt + delayMillis
            )
        }

        override suspend fun markFailed(action: OutboxActionEntity, errorMessage: String?, updatedAt: Long) {
            actions[action.id] = action.copy(
                status = OutboxActionStatus.FAILED,
                retryCount = action.retryCount + 1,
                lastError = errorMessage,
                updatedAt = updatedAt,
                availableAt = Long.MAX_VALUE
            )
        }

        override suspend fun resetStuck(cutoffTimestamp: Long, now: Long): Int = 0
    }

    private class MutableTimeProvider(var current: Long) : TimeProvider {
        override fun now(): Long = current
    }

    private companion object {
        private const val DEFAULT_ID = "action"
    }
}
