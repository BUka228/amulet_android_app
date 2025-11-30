package com.example.amulet.core.notifications

import android.content.Context
import android.content.Intent
import com.example.amulet.shared.core.logging.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Роутер push-уведомлений, полученных через OneSignal.
 *
 * На вход получает сырое data-payload и делегирует обработку
 * фиче-специфичным менеджерам (объятия, курсы, системы и т.п.).
 */
@Singleton
class PushNotificationRouter @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {

    fun handle(data: Map<String, String>) {
        when (data[KEY_TYPE]) {
            TYPE_HUG -> {
                Logger.d("Routing hug push via FGS: $data", tag = TAG)
                val intent = Intent("com.example.amulet.action.HUG_FROM_PUSH").apply {
                    // Явно указываем пакет приложения, чтобы интент не ушёл наружу.
                    setPackage(appContext.packageName)
                    putExtra("extra_hug_payload", HashMap(data))
                }
                appContext.startForegroundService(intent)
            }

            else -> {
                Logger.d("Unhandled push notification type=${data[KEY_TYPE]} data=$data", tag = TAG)
            }
        }
    }

    companion object {
        private const val TAG = "PushNotificationRouter"

        private const val KEY_TYPE = "type"
        private const val TYPE_HUG = "hug"
    }
}
