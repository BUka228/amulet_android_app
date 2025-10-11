package com.example.amulet_android_app

import android.app.Application
import com.example.amulet.core.auth.AppCheckInitializer
import com.example.amulet.core.telemetry.logging.TelemetryLoggingInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AmuletApp : Application() {

    @Inject
    lateinit var appCheckInitializer: AppCheckInitializer

    override fun onCreate() {
        super.onCreate()
        
        // Initialize App Check first, before other services
        appCheckInitializer.initialize(this, BuildConfig.DEBUG)
        
        // Initialize telemetry logging
        TelemetryLoggingInitializer.initialize(BuildConfig.DEBUG)
    }
}
