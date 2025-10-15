package com.example.amulet_android_app

import android.app.Application
import com.example.amulet.core.telemetry.logging.TelemetryInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AmuletApp : Application() {

    @Inject
    lateinit var telemetryInitializer: TelemetryInitializer

    override fun onCreate() {
        super.onCreate()

        telemetryInitializer.initialize(BuildConfig.DEBUG)
    }
}

