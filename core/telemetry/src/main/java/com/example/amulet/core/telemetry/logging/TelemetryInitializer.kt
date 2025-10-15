package com.example.amulet.core.telemetry.logging

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelemetryInitializer @Inject constructor(
    private val reporter: TelemetryReporter
) {

    fun initialize(isDebug: Boolean) {
        val baseAntilog = if (isDebug) {
            DebugAntilog()
        } else {
            TelemetryAntilog(reporter, ReleaseAntilog())
        }

        Napier.takeLogarithm()
        Napier.base(baseAntilog)
    }
}
