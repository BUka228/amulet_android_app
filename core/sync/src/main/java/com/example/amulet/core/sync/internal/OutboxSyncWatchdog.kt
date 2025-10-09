package com.example.amulet.core.sync.internal

import com.example.amulet.shared.core.logging.Logger
import javax.inject.Inject

class OutboxSyncWatchdog @Inject constructor(
    private val store: OutboxActionStore,
    private val timeProvider: TimeProvider,
    private val config: OutboxSyncConfig
) {

    suspend fun resetStuckActions(): Int {
        if (config.inFlightTimeoutMillis <= 0) {
            return 0
        }
        val now = timeProvider.now()
        val cutoff = now - config.inFlightTimeoutMillis
        if (cutoff <= 0) {
            return 0
        }
        val reset = store.resetStuck(cutoff, now)
        if (reset > 0) {
            Logger.w("Watchdog reset $reset outbox actions stuck in-flight", tag = TAG)
        }
        return reset
    }

    private companion object {
        private const val TAG = "OutboxWatchdog"
    }
}
