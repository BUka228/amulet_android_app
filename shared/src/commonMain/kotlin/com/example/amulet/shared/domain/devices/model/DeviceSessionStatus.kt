package com.example.amulet.shared.domain.devices.model

/**
 * Унифицированный статус сессии устройства для UI/foreground-слоёв.
 */
data class DeviceSessionStatus(
    val connection: BleConnectionState,
    val liveStatus: DeviceLiveStatus?,
)
