package com.example.amulet.core.sync.worker

import androidx.work.ListenableWorker
import com.example.amulet.core.database.entity.OutboxActionEntity
import com.example.amulet.core.sync.internal.ActionErrorResolver
import com.example.amulet.core.sync.internal.BackoffPolicy
import com.example.amulet.core.sync.internal.OutboxActionStore
import com.example.amulet.core.sync.internal.OutboxEngine
import com.example.amulet.core.sync.internal.OutboxSyncConfig
import com.example.amulet.core.sync.internal.OutboxSyncWatchdog
import com.example.amulet.core.sync.internal.TimeProvider
import com.example.amulet.core.sync.processing.ActionProcessor
import com.example.amulet.core.sync.processing.ActionProcessorFactory
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OutboxWorkerTest {

    @Test
    fun `runner resets watchdog and runs engine`() = runTest {
        val store = RecordingStore()
        val config = OutboxSyncConfig(
            maxActionsPerSync = 5,
            inFlightTimeoutMillis = 1L,
            baseBackoffMillis = 1_000L,
            maxBackoffMillis = 1_000L
        )
        val timeProvider = TimeProvider { 1_000L }
        val resolver = ActionErrorResolver(
            BackoffPolicy(
                baseDelayMillis = config.baseBackoffMillis,
                maxDelayMillis = config.maxBackoffMillis,
                jitterRatioMin = 1.0,
                jitterRatioMax = 1.0
            )
        )
        val engine = OutboxEngine(
            store = store,
            processorFactory = object : ActionProcessorFactory {
                override fun get(type: com.example.amulet.core.database.entity.OutboxActionType): ActionProcessor {
                    throw UnsupportedOperationException("no processors expected")
                }
            },
            errorResolver = resolver,
            timeProvider = timeProvider,
            config = config
        )
        val watchdog = OutboxSyncWatchdog(store, timeProvider, config)
        val runner = OutboxWorkerRunner(engine, watchdog, timeProvider)

        val result = runner.run()

        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals(1, store.resetCalls)
        assertEquals(1, store.takeCalls)
    }

    private class RecordingStore : OutboxActionStore {
        var resetCalls: Int = 0
        var takeCalls: Int = 0

        override suspend fun takeNextAction(now: Long): OutboxActionEntity? {
            takeCalls += 1
            return null
        }

        override suspend fun markCompleted(action: OutboxActionEntity, completedAt: Long) = Unit

        override suspend fun markForRetry(action: OutboxActionEntity, delayMillis: Long, errorMessage: String?, updatedAt: Long) = Unit

        override suspend fun markFailed(action: OutboxActionEntity, errorMessage: String?, updatedAt: Long) = Unit

        override suspend fun resetStuck(cutoffTimestamp: Long, now: Long): Int {
            resetCalls += 1
            return 0
        }
    }
}
