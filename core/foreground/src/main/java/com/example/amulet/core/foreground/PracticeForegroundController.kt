package com.example.amulet.core.foreground

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.Manifest
import androidx.core.app.NotificationCompat
import com.example.amulet.core.foreground.AmuletForegroundOrchestrator
import com.example.amulet.shared.domain.practices.PracticeSessionManager
import com.example.amulet.shared.domain.practices.model.PracticeId
import com.example.amulet.shared.domain.practices.model.PracticeSession
import com.example.amulet.shared.domain.practices.model.PracticeSessionId
import com.example.amulet.shared.domain.practices.model.PracticeSessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Контроллер foreground-логики для практик.
 * Инкапсулирует уведомление, обработку action-интентов и реакцию на activeSession.
 */
class PracticeForegroundController @Inject constructor(
    private val practiceSessionManager: PracticeSessionManager,
    private val orchestrator: AmuletForegroundOrchestrator,
) {

    private lateinit var service: Service

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun onCreate(service: Service) {
        this.service = service
        if (!hasRequiredPermissionsForConnectedDeviceFgs()) {
            // Нет необходимых runtime‑разрешений – не стартуем FGS, чтобы не уронить приложение.
            return
        }
        createNotificationChannel()
        val initial = buildPracticeNotification(null)
        service.startForeground(NOTIFICATION_ID, initial)
        observeActiveSession()
    }

    fun onDestroy() {
        scope.cancel()
    }

    fun handleIntent(action: String?) {
        when (action) {
            ACTION_PRACTICE_STOP -> {
                scope.launch {
                    val session = practiceSessionManager.activeSession.firstOrNull()
                    val sessionId = session?.id ?: return@launch
                    practiceSessionManager.stopSession(completed = true)
                    // дальнейшая остановка сервиса произойдёт через observeActiveSession
                }
            }
            ACTION_PRACTICE_OPEN -> {
                val launchIntent =
                    service.packageManager.getLaunchIntentForPackage(service.packageName)?.apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                if (launchIntent != null) {
                    service.startActivity(launchIntent)
                }
            }
        }
    }

    suspend fun startPractice(practiceId: PracticeId) {
        practiceSessionManager.startSession(practiceId)
    }

    suspend fun stopPractice(sessionId: PracticeSessionId, completed: Boolean) {
        val session = practiceSessionManager.activeSession.firstOrNull()
        if (session?.id != sessionId) return
        practiceSessionManager.stopSession(completed)
    }

    private fun observeActiveSession() {
        scope.launch {
            practiceSessionManager.activeSession.collect { session ->
                if (session == null) {
                    orchestrator.setPracticeActive(false)
                } else {
                    orchestrator.setPracticeActive(true)
                    updateNotificationForSession(session)
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(PRACTICES_CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    PRACTICES_CHANNEL_ID,
                    "Практики",
                    NotificationManager.IMPORTANCE_LOW
                )
                channel.description = "Foreground service for practice sessions"
                manager.createNotificationChannel(channel)
            }
        }
    }

    private fun buildPracticeNotification(session: PracticeSession?): Notification {
        val title = session?.let { "Практика" } ?: "Практика"

        val stopIntent = PendingIntent.getService(
            service,
            2,
            Intent(service, AmuletForegroundService::class.java).setAction(ACTION_PRACTICE_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openIntent = PendingIntent.getService(
            service,
            3,
            Intent(service, AmuletForegroundService::class.java).setAction(ACTION_PRACTICE_OPEN),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(service, PRACTICES_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("Идёт практика")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setOngoing(true)
            .setContentIntent(openIntent)

        builder.addAction(0, "Завершить", stopIntent)
        builder.addAction(0, "Открыть", openIntent)

        return builder.build()
    }

    private fun updateNotificationForSession(session: PracticeSession) {
        val notification = buildPracticeNotification(session)
        service.startForeground(NOTIFICATION_ID, notification)
    }

    private fun hasRequiredPermissionsForConnectedDeviceFgs(): Boolean {
        if (Build.VERSION.SDK_INT < 34) return true

        val hasFgs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            service.checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val transportPerms = listOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
        )

        val hasAnyTransport = transportPerms.any { perm ->
            service.checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED
        }

        return hasFgs && hasAnyTransport
    }

    companion object {
        const val PRACTICES_CHANNEL_ID = "amulet_practices_channel"
        const val NOTIFICATION_ID = 1

        const val ACTION_PRACTICE_STOP = "com.example.amulet.action.PRACTICE_STOP"
        const val ACTION_PRACTICE_OPEN = "com.example.amulet.action.PRACTICE_OPEN"
    }
}
