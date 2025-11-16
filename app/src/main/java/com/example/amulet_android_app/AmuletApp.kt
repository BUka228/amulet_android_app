package com.example.amulet_android_app

import android.app.Application
import androidx.lifecycle.lifecycleScope
import com.example.amulet.core.telemetry.logging.TelemetryInitializer
import com.example.amulet.shared.domain.initialization.DataInitializer
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class AmuletApp : Application() {

    @Inject
    lateinit var telemetryInitializer: TelemetryInitializer
    
    @Inject
    lateinit var dataInitializer: DataInitializer

    override fun onCreate() {
        super.onCreate()

        telemetryInitializer.initialize(BuildConfig.DEBUG)
        
        // Запускаем инициализацию данных при старте приложения
        CoroutineScope(Dispatchers.IO).launch {
            dataInitializer.initializeIfNeeded()
        }
    }
}

