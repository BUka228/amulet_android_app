package com.example.amulet.shared.domain.devices.model

/**
 * Статус BLE подключения.
 */
enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    FAILED
}

/**
 * Живой статус подключенного устройства (через BLE).
 */
data class DeviceLiveStatus(
    val serialNumber: String,
    val firmwareVersion: String,
    val hardwareVersion: Int,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val isOnline: Boolean
)
