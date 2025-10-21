package com.example.amulet.core.ble.internal

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Помощник для проверки разрешений Bluetooth.
 * 
 * Android 12+ требует новые разрешения:
 * - BLUETOOTH_SCAN
 * - BLUETOOTH_CONNECT
 * - BLUETOOTH_ADVERTISE (для периферийного режима)
 */
@Singleton
class BlePermissionsHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Проверить наличие всех необходимых разрешений для BLE.
     */
    fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+
            hasPermission(Manifest.permission.BLUETOOTH_SCAN) &&
            hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            // Android 11 и ниже
            hasPermission(Manifest.permission.BLUETOOTH) &&
            hasPermission(Manifest.permission.BLUETOOTH_ADMIN) &&
            hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    
    /**
     * Получить список необходимых разрешений для текущей версии Android.
     */
    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }
    
    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}
