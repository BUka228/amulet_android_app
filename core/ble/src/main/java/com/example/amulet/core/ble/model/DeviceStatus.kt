package com.example.amulet.core.ble.model

/**
 * Статус физического устройства амулета.
 */
data class DeviceStatus(
    val firmwareVersion: String,
    val hardwareVersion: Int,
    val batteryLevel: Int, // 0-100
    val isCharging: Boolean,
    val isOnline: Boolean,
    val lastSeen: Long // timestamp
)

/**
 * Состояние готовности устройства (Flow Control).
 */
sealed interface DeviceReadyState {
    /** Устройство готово принять данные */
    data object ReadyForData : DeviceReadyState
    
    /** Устройство обрабатывает данные */
    data object Processing : DeviceReadyState
    
    /** Устройство занято */
    data object Busy : DeviceReadyState
    
    /** Ошибка на устройстве */
    data class Error(val code: String, val message: String) : DeviceReadyState
}
