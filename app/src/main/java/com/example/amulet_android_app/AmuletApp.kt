package com.example.amulet_android_app

import android.app.Application
import com.example.amulet.core.notifications.OneSignalManager
import com.example.amulet.core.telemetry.logging.TelemetryInitializer
import com.example.amulet.shared.core.logging.Logger
import com.example.amulet.shared.domain.notifications.model.PushTokenRegistration
import com.example.amulet.shared.domain.notifications.usecase.RegisterPushTokenUseCase
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import com.github.michaelbull.result.onFailure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime

private const val NOTIFICATION_PLATFORM = "onesignal"

@HiltAndroidApp
class AmuletApp : Application() {

    @Inject
    lateinit var telemetryInitializer: TelemetryInitializer

    @Inject
    lateinit var oneSignalManager: OneSignalManager

    @Inject
    lateinit var registerPushTokenUseCase: RegisterPushTokenUseCase

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        telemetryInitializer.initialize(BuildConfig.DEBUG)
        initializeNotifications()
    }

    private fun initializeNotifications() {
        if (BuildConfig.ONESIGNAL_APP_ID.isBlank()) return

        oneSignalManager.initialize(BuildConfig.ONESIGNAL_APP_ID, BuildConfig.DEBUG)
        oneSignalManager.requestNotificationPermission()

        applicationScope.launch {
            oneSignalManager.playerId()
                .filterNotNull()
                .filter { it.isNotBlank() }
                .distinctUntilChanged()
                .collectLatest { playerId ->
                    registerPlayerId(playerId)
                }
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun registerPlayerId(playerId: String) {
        val registration = PushTokenRegistration(
            token = playerId,
            platform = NOTIFICATION_PLATFORM,
            lastSeenAt = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        )
        registerPushTokenUseCase(registration).onFailure { error ->
            Logger.w(
                message = "Failed to register OneSignal playerId: $playerId due to $error",
                tag = "AmuletApp"
            )
        }
    }
}
