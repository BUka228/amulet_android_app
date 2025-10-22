package com.example.amulet.shared.domain.devices.model

import kotlinx.serialization.Serializable


@Serializable
data class Device(
    val id: DeviceId,
    val ownerId: String,
    val serialNumber: String,
    val hardwareVersion: Int,
    val firmwareVersion: String,
    val name: String?,
    val batteryLevel: Double?,
    val status: DeviceStatus,
    val pairedAt: Long,
    val settings: DeviceSettings
)

@Serializable
data class DeviceSettings(
    val brightness: Double = 1.0,
    val haptics: Double = 0.5,
    val gestures: Map<String, String> = emptyMap()
)

enum class DeviceStatus {
    ONLINE,
    OFFLINE,
    CHARGING,
    UNKNOWN;
    
    companion object {
        fun fromString(value: String?): DeviceStatus = when (value?.lowercase()) {
            "online" -> ONLINE
            "offline" -> OFFLINE
            "charging" -> CHARGING
            else -> UNKNOWN
        }
    }
}
