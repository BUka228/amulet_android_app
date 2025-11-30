package com.example.amulet.core.foreground

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.amulet.core.notifications.HugNotificationContentProvider
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.logging.Logger
import com.example.amulet.shared.domain.hugs.ExecuteRemoteHugCommandUseCase
import com.example.amulet.shared.domain.hugs.model.Emotion
import com.example.amulet.shared.domain.hugs.model.HugId
import com.example.amulet.shared.domain.hugs.model.PairId
import com.example.amulet.shared.domain.hugs.model.RemoteHugCommand
import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.user.model.UserId
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Контроллер foreground-логики для объятий.
 * Отвечает за выполнение команды удалённого «объятия» под защитой FGS.
 */
class HugsForegroundController @Inject constructor(
    private val executeRemoteHugCommandUseCase: ExecuteRemoteHugCommandUseCase,
    private val orchestrator: AmuletForegroundOrchestrator,
) {

    private lateinit var service: Service

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun onCreate(service: Service) {
        this.service = service
        Logger.d("HugsForegroundController.onCreate", tag = TAG)
    }

    fun onDestroy() {
        Logger.d("HugsForegroundController.onDestroy", tag = TAG)
        scope.cancel()
    }

    fun handleHugFromPush(payload: Map<String, String>?) {
        if (payload == null) {
            Logger.e("handleHugFromPush called with null payload", tag = TAG)
            return
        }

        val command = parseRemoteHugCommand(payload) ?: run {
            Logger.e("Invalid hug push payload: $payload", tag = TAG)
            return
        }

        Logger.d("Starting remote hug execution in FGS: $command", tag = TAG)
        scope.launch {
            orchestrator.setHugActive(true)
            try {
                executeWithRetry(command)
            } catch (t: Throwable) {
                Logger.e("Unexpected error during remote hug execution", throwable = t, tag = TAG)
            } finally {
                orchestrator.setHugActive(false)
            }
        }
    }

    private fun parseRemoteHugCommand(data: Map<String, String>): RemoteHugCommand? {
        val fromUserId = data[KEY_FROM_USER_ID]?.takeIf { it.isNotBlank() }?.let(::UserId)
            ?: return null

        val hugId = data[KEY_HUG_ID]?.takeIf { it.isNotBlank() }?.let(::HugId)
        val pairId = data[KEY_PAIR_ID]?.takeIf { it.isNotBlank() }?.let(::PairId)
        val toUserId = data[KEY_TO_USER_ID]?.takeIf { it.isNotBlank() }?.let(::UserId)

        val colorHex = data[KEY_EMOTION_COLOR]
        val emotionPatternId = data[KEY_EMOTION_PATTERN_ID]?.takeIf { it.isNotBlank() }?.let(::PatternId)
        val emotion = if (colorHex != null) {
            Emotion(colorHex = colorHex, patternId = emotionPatternId)
        } else {
            null
        }

        val patternIdOverride = data[KEY_PATTERN_ID]?.takeIf { it.isNotBlank() }?.let(::PatternId)

        return RemoteHugCommand(
            hugId = hugId,
            pairId = pairId,
            fromUserId = fromUserId,
            toUserId = toUserId,
            emotion = emotion,
            patternIdOverride = patternIdOverride,
            payload = data,
        )
    }

    private suspend fun executeWithRetry(command: RemoteHugCommand) {
        val maxAttempts = 3
        var attempt = 1
        var delayMs = 5_000L
        var lastError: AppError? = null

        while (attempt <= maxAttempts) {
            Logger.d("Executing remote hug (attempt=$attempt/$maxAttempts)", tag = TAG)
            var shouldRetry = false

            val result = executeRemoteHugCommandUseCase(command)
            result.onSuccess {
                Logger.d("Remote hug executed successfully in FGS (attempt=$attempt)", tag = TAG)
            }.onFailure { error ->
                lastError = error
                val isSoftBleError = when (error) {
                    is AppError.BleError.DeviceNotFound,
                    is AppError.BleError.DeviceDisconnected -> true
                    else -> false
                }

                if (isSoftBleError && attempt < maxAttempts) {
                    shouldRetry = true
                    Logger.d(
                        "Remote hug failed with BLE connectivity error, will retry after ${delayMs}ms: $error",
                        tag = TAG
                    )
                } else {
                    Logger.e(
                        "Remote hug execution failed in FGS (attempt=$attempt/$maxAttempts): $error",
                        tag = TAG
                    )
                }
            }

            if (!shouldRetry) {
                return
            }

            delay(delayMs)
            attempt += 1
            delayMs *= 2
        }

        val error = lastError
        if (error is AppError.BleError.DeviceNotFound || error is AppError.BleError.DeviceDisconnected) {
            showHugQueuedNotification(command)
        }
    }

    private fun showHugQueuedNotification(command: RemoteHugCommand) {
        val hugId = command.hugId?.value
        val emotionColor = command.emotion?.colorHex
        val content = HugNotificationContentProvider.queuedHug(
            emotionName = null,
            emotionColorHex = emotionColor,
        )

        val manager = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (manager.getNotificationChannel(HUGS_CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    HUGS_CHANNEL_ID,
                    "Объятия",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                manager.createNotificationChannel(channel)
            }
        }

        val uri = if (hugId != null) {
            Uri.parse("amulet://hugs/$hugId")
        } else {
            Uri.parse("amulet://hugs")
        }

        val launchIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            service,
            1001,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(service, HUGS_CHANNEL_ID)
            .setContentTitle(content.title)
            .setContentText(content.message)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationId = HUGS_NOTIFICATION_ID_BASE + (hugId?.hashCode() ?: 0)
        manager.notify(notificationId, notification)
    }

    companion object {
        private const val TAG = "HugsForegroundController"

        private const val HUGS_CHANNEL_ID = "amulet_hugs_channel"
        private const val HUGS_NOTIFICATION_ID_BASE = 2000

        private const val KEY_HUG_ID = "hugId"
        private const val KEY_PAIR_ID = "pairId"
        private const val KEY_FROM_USER_ID = "fromUserId"
        private const val KEY_TO_USER_ID = "toUserId"
        private const val KEY_EMOTION_COLOR = "emotionColor"
        private const val KEY_EMOTION_PATTERN_ID = "emotionPatternId"
        private const val KEY_PATTERN_ID = "patternId"
    }
}
