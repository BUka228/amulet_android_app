package com.example.amulet.data.devices.mapper

import com.example.amulet.core.database.entity.DeviceEntity
import com.example.amulet.core.database.entity.DeviceStatus as EntityDeviceStatus
import com.example.amulet.shared.domain.devices.model.Device
import com.example.amulet.shared.domain.devices.model.DeviceId
import com.example.amulet.shared.domain.devices.model.DeviceSettings
import com.example.amulet.shared.domain.devices.model.DeviceStatus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Extension функции для маппинга Device между слоями.
 */

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

/**
 * Entity -> Domain
 */
fun DeviceEntity.toDevice(): Device {
    val settings = try {
        json.decodeFromString<DeviceSettings>(settingsJson)
    } catch (e: Exception) {
        DeviceSettings()
    }
    
    return Device(
        id = DeviceId(id),
        ownerId = ownerId,
        bleAddress = bleAddress,
        hardwareVersion = hardwareVersion,
        firmwareVersion = firmwareVersion ?: "unknown",
        name = name,
        batteryLevel = batteryLevel,
        status = status.toDomainStatus(),
        addedAt = addedAt,
        lastConnectedAt = lastConnectedAt,
        settings = settings
    )
}

/**
 * Domain -> Entity
 */
fun Device.toDeviceEntity(): DeviceEntity {
    val settingsJson = json.encodeToString(settings)
    
    return DeviceEntity(
        id = id.value,
        ownerId = ownerId,
        bleAddress = bleAddress,
        hardwareVersion = hardwareVersion,
        firmwareVersion = firmwareVersion,
        name = name,
        batteryLevel = batteryLevel,
        status = status.toEntityStatus(),
        settingsJson = settingsJson,
        addedAt = addedAt,
        lastConnectedAt = lastConnectedAt
    )
}

private fun EntityDeviceStatus?.toDomainStatus(): DeviceStatus {
    return when (this) {
        EntityDeviceStatus.ONLINE -> DeviceStatus.ONLINE
        EntityDeviceStatus.OFFLINE -> DeviceStatus.OFFLINE
        EntityDeviceStatus.CHARGING -> DeviceStatus.CHARGING
        EntityDeviceStatus.ERROR, EntityDeviceStatus.BANNED, null -> DeviceStatus.UNKNOWN
    }
}

private fun DeviceStatus.toEntityStatus(): EntityDeviceStatus? {
    return when (this) {
        DeviceStatus.ONLINE -> EntityDeviceStatus.ONLINE
        DeviceStatus.OFFLINE -> EntityDeviceStatus.OFFLINE
        DeviceStatus.CHARGING -> EntityDeviceStatus.CHARGING
        DeviceStatus.UNKNOWN -> null
    }
}
