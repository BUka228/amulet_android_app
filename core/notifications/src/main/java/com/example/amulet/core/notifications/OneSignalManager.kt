package com.example.amulet.core.notifications

import android.app.Application
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import com.onesignal.user.subscriptions.IPushSubscriptionObserver
import com.onesignal.user.subscriptions.PushSubscriptionChangedState
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Singleton
class OneSignalManager @Inject constructor(
    private val application: Application
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val playerIdFlow = MutableStateFlow<String?>(null)
    private val pushSubscriptionObserver = object : IPushSubscriptionObserver {
        override fun onPushSubscriptionChange(state: PushSubscriptionChangedState) {
            playerIdFlow.value = state.current.id
        }
    }

    fun initialize(appId: String, isDebug: Boolean) {
        if (appId.isBlank()) return
        OneSignal.Debug.logLevel = if (isDebug) LogLevel.VERBOSE else LogLevel.NONE
        OneSignal.initWithContext(application, appId)

        updatePlayerId()

        OneSignal.User.pushSubscription.addObserver(pushSubscriptionObserver)
    }

    fun refreshPlayerId() {
        updatePlayerId()
    }

    fun requestNotificationPermission() {
        scope.launch {
            runCatching {
                OneSignal.Notifications.requestPermission(true)
            }
        }
    }

    fun playerId(): StateFlow<String?> = playerIdFlow.asStateFlow()

    private fun updatePlayerId() {
        playerIdFlow.value = OneSignal.User.pushSubscription.id
    }
}
