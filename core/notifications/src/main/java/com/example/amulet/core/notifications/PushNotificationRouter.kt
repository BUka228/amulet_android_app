package com.example.amulet.core.notifications

import android.content.Context
import android.content.Intent
import com.example.amulet.shared.core.logging.Logger
import com.example.amulet.shared.domain.hugs.ExecuteRemoteHugCommandUseCase
import com.example.amulet.shared.domain.hugs.model.RemoteHugCommand
import com.example.amulet.shared.domain.hugs.model.Emotion
import com.example.amulet.shared.domain.hugs.model.HugId
import com.example.amulet.shared.domain.hugs.model.PairId
import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.user.model.UserId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
    private val executeRemoteHugCommand: ExecuteRemoteHugCommandUseCase,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun handle(data: Map<String, String>) {
        when (data[KEY_TYPE]) {
            TYPE_HUG -> {
                Logger.d("Received hug push: $data", tag = TAG)
                handleHugPush(data)
            }

            else -> {
                Logger.d("Unhandled push notification type=${data[KEY_TYPE]} data=$data", tag = TAG)
            }
        }
    }

    private fun handleHugPush(data: Map<String, String>) {
        // Парсим минимальный RemoteHugCommand из payload.
        // Ожидается, что бэкенд передаёт идентификаторы в data-полях.
        val hugId = data[KEY_HUG_ID]?.let { HugId(it) }
        val pairId = data[KEY_PAIR_ID]?.let { PairId(it) }
        val fromUserId = data[KEY_FROM_USER_ID]?.let { UserId(it) }

        if (fromUserId == null) {
            Logger.w("Hug push is missing fromUserId, skipping: $data", tag = TAG)
            return
        }

        val toUserId = data[KEY_TO_USER_ID]?.let { UserId(it) }
        val patternIdOverride = data[KEY_PATTERN_ID]?.let { PatternId(it) }

        val emotion = if (data[KEY_EMOTION_COLOR] != null || patternIdOverride != null) {
            Emotion(
                colorHex = data[KEY_EMOTION_COLOR] ?: "#FFFFFF",
                patternId = patternIdOverride,
            )
        } else {
            null
        }

        val command = RemoteHugCommand(
            hugId = hugId,
            pairId = pairId,
            fromUserId = fromUserId,
            toUserId = toUserId,
            emotion = emotion,
            patternIdOverride = patternIdOverride,
            payload = null,
        )

        scope.launch {
            val result = executeRemoteHugCommand(command)
            val error = result.component2()
            if (error != null) {
                Logger.w("Failed to execute remote hug command from push: $error", tag = TAG)
            }
        }
    }

    companion object {
        private const val TAG = "PushNotificationRouter"

        private const val KEY_TYPE = "type"
        private const val TYPE_HUG = "hug"

        private const val KEY_HUG_ID = "hugId"
        private const val KEY_PAIR_ID = "pairId"
        private const val KEY_FROM_USER_ID = "fromUserId"
        private const val KEY_TO_USER_ID = "toUserId"
        private const val KEY_PATTERN_ID = "patternId"
        private const val KEY_EMOTION_COLOR = "emotionColorHex"
    }
}
