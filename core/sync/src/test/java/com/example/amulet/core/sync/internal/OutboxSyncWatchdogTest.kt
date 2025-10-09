package com.example.amulet.core.sync.internal

import com.example.amulet.core.database.entity.OutboxActionEntity
import com.example.amulet.core.database.entity.OutboxActionStatus
import com.example.amulet.core.database.entity.OutboxActionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class OutboxSyncWatchdogTest {

    private val config = OutboxSyncConfig(
        maxActionsPerSync = 10,
        inFlightTimeoutMillis = 5_000L,
        baseBackoffMillis = 1_000L,
        maxBackoffMillis = 8_000L
    )

    @Test
    fun `reset stuck actions moves them back to pending`() = runTest {
        val initial = OutboxActionEntity(
            id = "a1",
            type = OutboxActionType.HUG_SEND,
            payloadJson = "{}",
            status = OutboxActionStatus.IN_FLIGHT,
            retryCount = 0,
            lastError = null,
            idempotencyKey = null,
            createdAt = 0L,
            updatedAt = 0L,
            availableAt = 0L,
            priority = 0,
            targetEntityId = null
        )
        val store = RecordingStore(initial)
        val timeProvider = object : TimeProvider {
            override fun now(): Long = 10_000L
        }
        val watchdog = OutboxSyncWatchdog(store, timeProvider, config)

        val reset = watchdog.resetStuckActions()

        assertEquals(1, reset)
        val updated = store.actions[singleKey]!!
        assertEquals(OutboxActionStatus.PENDING, updated.status)
    }

    private class RecordingStore(action: OutboxActionEntity) : OutboxActionStore {
        val actions = mutableMapOf(singleKey to action)

        override suspend fun takeNextAction(now: Long): OutboxActionEntity? = null

        override suspend fun markCompleted(action: OutboxActionEntity, completedAt: Long) = Unit

        override suspend fun markForRetry(action: OutboxActionEntity, delayMillis: Long, errorMessage: String?, updatedAt: Long) = Unit

        override suspend fun markFailed(action: OutboxActionEntity, errorMessage: String?, updatedAt: Long) = Unit

        override suspend fun resetStuck(cutoffTimestamp: Long, now: Long): Int {
            val current = actions[singleKey] ?: return 0
            if (current.status == OutboxActionStatus.IN_FLIGHT && current.updatedAt < cutoffTimestamp) {
                actions[singleKey] = current.copy(
                    status = OutboxActionStatus.PENDING,
                    updatedAt = now,
                    availableAt = now
                )
                return 1
            }
            return 0
        }
    }

    private companion object {
        private const val singleKey = "a1"
    }
}
