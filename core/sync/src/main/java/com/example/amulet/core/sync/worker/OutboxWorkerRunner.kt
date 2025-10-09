package com.example.amulet.core.sync.worker

import androidx.work.ListenableWorker
import com.example.amulet.core.sync.internal.OutboxEngine
import com.example.amulet.core.sync.internal.OutboxSyncWatchdog
import com.example.amulet.core.sync.internal.TimeProvider
import com.example.amulet.shared.core.logging.Logger
import javax.inject.Inject

class OutboxWorkerRunner @Inject constructor(
    private val engine: OutboxEngine,
    private val watchdog: OutboxSyncWatchdog,
    private val timeProvider: TimeProvider
) {

    suspend fun run(): ListenableWorker.Result = try {
        val resetCount = watchdog.resetStuckActions()
        if (resetCount > 0) {
            Logger.w("Reset $resetCount stuck outbox actions", tag = TAG)
        }

        val processed = engine.run()
        if (processed > 0) {
            Logger.d("Processed $processed outbox actions", tag = TAG)
        } else {
            Logger.d("No due outbox actions to process", tag = TAG)
        }

        ListenableWorker.Result.success()
    } catch (t: Throwable) {
        val message = "OutboxWorker failed at ${timeProvider.now()}: ${t.message}"
        Logger.e(message, t, TAG)
        ListenableWorker.Result.failure()
    }

    private companion object {
        private const val TAG = "OutboxWorkerRunner"
    }
}
