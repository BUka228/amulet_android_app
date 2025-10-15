package com.example.amulet.core.telemetry.logging

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TelemetryAntilog @Inject constructor(
    private val reporter: TelemetryReporter,
    private val delegate: Antilog,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : Antilog() {

    override fun performLog(priority: LogLevel, tag: String?, throwable: Throwable?, message: String?) {
        delegate.log(priority, tag, throwable, message)

        if (priority.ordinal < LogLevel.WARNING.ordinal) return

        scope.launch {
            runCatching {
                reporter.reportLog(priority, tag, message, throwable)
            }
        }
    }
}
