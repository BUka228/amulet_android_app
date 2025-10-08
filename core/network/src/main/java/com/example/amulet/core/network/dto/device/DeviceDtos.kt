package com.example.amulet.core.network.dto.device

import com.example.amulet.core.network.dto.common.CursorPageDto
import com.example.amulet.core.network.serialization.ApiTimestamp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class DeviceDto(
    val id: String,
    val ownerId: String? = null,
    val serial: String? = null,
    val hardwareVersion: Int? = null,
    val firmwareVersion: String? = null,
    val name: String? = null,
    val batteryLevel: Double? = null,
    val status: String? = null,
    val pairedAt: ApiTimestamp? = null,
    val settings: DeviceSettingsDto? = null
)

@Serializable
data class DeviceSettingsDto(
    val brightness: Double? = null,
    val haptics: Double? = null,
    val gestures: JsonObject? = null
)

@Serializable
data class DeviceResponseDto(
    val device: DeviceDto
)

@Serializable
data class DevicesResponseDto(
    val devices: List<DeviceDto>
)

@Serializable
data class DeviceClaimRequestDto(
    val serial: String,
    val claimToken: String,
    val name: String? = null
)

@Serializable
data class DeviceUpdateRequestDto(
    val name: String? = null,
    val settings: DeviceSettingsDto? = null
)

@Serializable
data class DeviceUnclaimResponseDto(
    val ok: Boolean
)

@Serializable
data class DeviceAuditResponseDto(
    val data: CursorPageDto<JsonObject>? = null
)
