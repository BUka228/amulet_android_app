package com.example.amulet.core.foreground

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Android-обёртка для запуска AmuletForegroundService при начале сессии практики.
 */
class PracticeForegroundLauncher @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun ensureServiceStarted() {
        if (!hasRequiredPermissionsForConnectedDeviceFgs()) {
            // Нет необходимых runtime‑разрешений – не стартуем FGS, чтобы не словить ForegroundServiceDidNotStartInTimeException.
            return
        }

        val intent = Intent(context, AmuletForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun hasRequiredPermissionsForConnectedDeviceFgs(): Boolean {
        if (Build.VERSION.SDK_INT < 34) return true

        val hasFgs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE,
            ) == PackageManager.PERMISSION_GRANTED
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
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }

        return hasFgs && hasAnyTransport
    }
}
