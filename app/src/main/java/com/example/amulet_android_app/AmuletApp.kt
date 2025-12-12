package com.example.amulet_android_app

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.amulet.core.notifications.OneSignalManager
import com.example.amulet.core.notifications.OneSignalUserBindingManager
import com.example.amulet.core.notifications.PushTokenSyncManager
import com.example.amulet.core.telemetry.logging.TelemetryInitializer
import com.example.amulet.shared.domain.devices.usecase.AutoConnectLastDeviceUseCase
import com.example.amulet.shared.domain.initialization.DataInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class AmuletApp : Application(), Configuration.Provider {

    @Inject
    lateinit var telemetryInitializer: TelemetryInitializer
    
    @Inject
    lateinit var dataInitializer: DataInitializer

    @Inject
    lateinit var oneSignalManager: OneSignalManager

    @Inject
    lateinit var pushTokenSyncManager: PushTokenSyncManager

    // Сам по себе не используется, но инстанцирование через DI запускает биндинг OneSignal<->User.
    @Inject
    lateinit var oneSignalUserBindingManager: OneSignalUserBindingManager

    @Inject
    lateinit var autoConnectLastDeviceUseCase: AutoConnectLastDeviceUseCase

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        telemetryInitializer.initialize(BuildConfig.DEBUG)

        oneSignalManager.initialize(BuildConfig.ONESIGNAL_APP_ID, BuildConfig.DEBUG)
        pushTokenSyncManager.refresh()
        
        // Запускаем инициализацию данных при старте приложения
        CoroutineScope(Dispatchers.IO).launch {
            dataInitializer.initializeIfNeeded()
            autoConnectLastDeviceUseCase()
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.VERBOSE else Log.INFO)
            .build()
}

