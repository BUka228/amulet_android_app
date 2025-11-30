package com.example.amulet_android_app

import android.app.Application
import com.example.amulet.core.notifications.OneSignalManager
import com.example.amulet.core.notifications.OneSignalUserBindingManager
import com.example.amulet.core.notifications.PushTokenSyncManager
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

    @Inject
    lateinit var oneSignalManager: OneSignalManager

    @Inject
    lateinit var pushTokenSyncManager: PushTokenSyncManager

    // Сам по себе не используется, но инстанцирование через DI запускает биндинг OneSignal<->User.
    @Inject
    lateinit var oneSignalUserBindingManager: OneSignalUserBindingManager

    override fun onCreate() {
        super.onCreate()

        telemetryInitializer.initialize(BuildConfig.DEBUG)

        oneSignalManager.initialize(BuildConfig.ONESIGNAL_APP_ID, BuildConfig.DEBUG)
        pushTokenSyncManager.refresh()
        
        // Запускаем инициализацию данных при старте приложения
        CoroutineScope(Dispatchers.IO).launch {
            dataInitializer.initializeIfNeeded()
        }
    }
}

