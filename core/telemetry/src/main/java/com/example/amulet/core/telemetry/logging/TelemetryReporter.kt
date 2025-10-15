package com.example.amulet.core.telemetry.logging

import io.github.aakira.napier.LogLevel

interface TelemetryReporter {
    suspend fun reportLog(level: LogLevel, tag: String?, message: String?, throwable: Throwable?)
}
