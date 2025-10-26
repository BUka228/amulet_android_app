package com.example.amulet.shared.domain.devices.model

import com.example.amulet.shared.core.AppError

/**
 * Единое состояние BLE подключения к устройству.
 */
sealed interface BleConnectionState {
    /** Устройство отключено */
    data object Disconnected : BleConnectionState
    
    /** Идет процесс подключения */
    data object Connecting : BleConnectionState
    
    /** Устройство подключено и готово к работе */
    data object Connected : BleConnectionState
    
    /** Попытка переподключения */
    data class Reconnecting(val attempt: Int) : BleConnectionState
    
    /** Подключение не удалось */
    data class Failed(val error: AppError) : BleConnectionState
}

/**
 * Живой статус подключенного устройства (через BLE).
 */
data class DeviceLiveStatus(
    val firmwareVersion: String,
    val hardwareVersion: Int,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val isOnline: Boolean
)
