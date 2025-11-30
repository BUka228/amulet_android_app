package com.example.amulet.core.notifications

import android.app.Application
import android.content.Intent
import android.net.Uri
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import com.onesignal.notifications.INotificationClickEvent
import com.onesignal.notifications.INotificationClickListener
import com.onesignal.notifications.INotificationLifecycleListener
import com.onesignal.notifications.INotificationWillDisplayEvent
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
    private val application: Application,
    private val pushNotificationRouter: PushNotificationRouter,
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

        // Обработка пуша, когда приложение на переднем плане.
        val foregroundListener = object : INotificationLifecycleListener {
            override fun onWillDisplay(event: INotificationWillDisplayEvent) {
                val additional = event.notification.additionalData
                val data: Map<String, String> =
                    (additional as? Map<*, *>)
                        ?.mapNotNull { (k, v) ->
                            (k as? String)?.let { key -> key to (v?.toString() ?: "") }
                        }
                        ?.toMap()
                        ?: emptyMap()

                pushNotificationRouter.handle(data)
                // Ничего не блокируем: уведомление покажется как обычно.
            }
        }
        OneSignal.Notifications.addForegroundLifecycleListener(foregroundListener)

        // Обработка клика по уведомлению (deeplink в экран объятий / конкретное объятие).
        val clickListener = object : INotificationClickListener {
            override fun onClick(event: INotificationClickEvent) {
                val additional = event.notification.additionalData
                val data: Map<String, String> =
                    (additional as? Map<*, *>)
                        ?.mapNotNull { (k, v) ->
                            (k as? String)?.let { key -> key to (v?.toString() ?: "") }
                        }
                        ?.toMap()
                        ?: emptyMap()

                val type = data["type"]
                val hugId = data["hugId"]

                val uri = if (type == "hug" && !hugId.isNullOrBlank()) {
                    Uri.parse("amulet://hugs/$hugId")
                } else {
                    Uri.parse("amulet://hugs")
                }

                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                application.startActivity(intent)
            }
        }
        OneSignal.Notifications.addClickListener(clickListener)
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

    /**
     * Привязать текущего авторизованного пользователя к профилю OneSignal.
     * userId — доменный/бэкенд-идентификатор пользователя.
     */
    fun login(userId: String) {
        if (userId.isBlank()) return
        OneSignal.login(userId)
    }

    /**
     * Отвязать пользователя от OneSignal (например, при выходе из аккаунта).
     */
    fun logout() {
        OneSignal.logout()
    }

    private fun updatePlayerId() {
        playerIdFlow.value = OneSignal.User.pushSubscription.id
    }
}
