package com.example.amulet_android_app

import android.app.Application
import com.example.amulet.core.telemetry.logging.TelemetryLoggingInitializer
import com.example.amulet_android_app.BuildConfig
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AmuletApp : Application() {

    override fun onCreate() {
        super.onCreate()
        TelemetryLoggingInitializer.initialize(BuildConfig.DEBUG)
    }
}
