package com.example.amulet.core.telemetry.logging

import com.google.firebase.crashlytics.FirebaseCrashlytics

interface CrashlyticsReporter {
    fun setCustomKey(key: String, value: String)
    fun log(message: String)
    fun recordException(throwable: Throwable)
}

class FirebaseCrashlyticsReporter(
    private val crashlytics: FirebaseCrashlytics
) : CrashlyticsReporter {
    override fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }

    override fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }
}
