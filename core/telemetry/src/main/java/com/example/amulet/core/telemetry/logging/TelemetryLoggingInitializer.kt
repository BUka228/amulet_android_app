package com.example.amulet.core.telemetry.logging

import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier


object TelemetryLoggingInitializer {

    fun initialize(
        isDebug: Boolean,
        crashlytics: CrashlyticsReporter = FirebaseCrashlyticsReporter(FirebaseCrashlytics.getInstance())
    ) {
        val baseAntilog = if (isDebug) DebugAntilog() else ReleaseAntilog()

        Napier.takeLogarithm()
        Napier.base(CrashlyticsAntilog(crashlytics, baseAntilog))
    }
}
