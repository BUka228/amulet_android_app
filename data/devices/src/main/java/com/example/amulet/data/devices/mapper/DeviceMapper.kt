package com.example.amulet.data.devices.mapper

import com.example.amulet.core.database.entity.DeviceEntity
import com.example.amulet.core.database.entity.DeviceStatus as EntityDeviceStatus
import com.example.amulet.core.network.dto.device.DeviceDto
import com.example.amulet.core.network.dto.device.DeviceSettingsDto
import com.example.amulet.shared.domain.devices.model.Device
import com.example.amulet.shared.domain.devices.model.DeviceId
import com.example.amulet.shared.domain.devices.model.DeviceSettings
import com.example.amulet.shared.domain.devices.model.DeviceStatus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Маппер для преобразования Device между слоями.
 */
class DeviceMapper @Inject constructor(
    private val json: Json
) {
    
    /**
     * DTO -> Domain
     */
    fun toDomain(dto: DeviceDto): Device {
        return Device(
            id = DeviceId(dto.id),
            ownerId = dto.ownerId ?: "",
            serialNumber = dto.serial ?: "",
            hardwareVersion = dto.hardwareVersion ?: 100,
            firmwareVersion = dto.firmwareVersion ?: "unknown",
            name = dto.name,
            batteryLevel = dto.batteryLevel,
            status = mapStatus(dto.status),
            pairedAt = dto.pairedAt?.epochMillis ?: System.currentTimeMillis(),
            settings = mapSettings(dto.settings)
        )
    }
    
    /**
     * Entity -> Domain
     */
    fun toDomain(entity: DeviceEntity): Device {
        val settings = try {
            json.decodeFromString<DeviceSettings>(entity.settingsJson)
        } catch (e: Exception) {
            DeviceSettings()
        }
        
        return Device(
            id = DeviceId(entity.id),
            ownerId = entity.ownerId,
            serialNumber = entity.serial,
            hardwareVersion = entity.hardwareVersion,
            firmwareVersion = entity.firmwareVersion ?: "unknown",
            name = entity.name,
            batteryLevel = entity.batteryLevel,
            status = mapEntityStatus(entity.status),
            pairedAt = entity.pairedAt ?: System.currentTimeMillis(),
            settings = settings
        )
    }
    
    /**
     * DTO -> Entity
     */
    fun toEntity(dto: DeviceDto): DeviceEntity {
        val settingsJson = dto.settings?.let { settingsDto ->
            json.encodeToString(mapSettings(settingsDto))
        } ?: json.encodeToString(DeviceSettings())
        
        return DeviceEntity(
            id = dto.id,
            ownerId = dto.ownerId ?: "",
            serial = dto.serial ?: "",
            hardwareVersion = dto.hardwareVersion ?: 100,
            firmwareVersion = dto.firmwareVersion,
            name = dto.name,
            batteryLevel = dto.batteryLevel,
            status = mapStatusToEntity(dto.status),
            settingsJson = settingsJson,
            pairedAt = dto.pairedAt?.epochMillis,
            updatedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Domain -> Entity
     */
    fun toEntity(domain: Device): DeviceEntity {
        val settingsJson = json.encodeToString(domain.settings)
        
        return DeviceEntity(
            id = domain.id.value,
            ownerId = domain.ownerId,
            serial = domain.serialNumber,
            hardwareVersion = domain.hardwareVersion,
            firmwareVersion = domain.firmwareVersion,
            name = domain.name,
            batteryLevel = domain.batteryLevel,
            status = mapDomainStatusToEntity(domain.status),
            settingsJson = settingsJson,
            pairedAt = domain.pairedAt,
            updatedAt = System.currentTimeMillis()
        )
    }
    
    private fun mapSettings(dto: DeviceSettingsDto?): DeviceSettings {
        if (dto == null) return DeviceSettings()
        
        val gesturesMap = dto.gestures?.mapValues { it.value.toString() } ?: emptyMap()
        
        return DeviceSettings(
            brightness = dto.brightness ?: 1.0,
            haptics = dto.haptics ?: 0.5,
            gestures = gesturesMap
        )
    }
    
    private fun mapStatus(status: String?): DeviceStatus {
        return DeviceStatus.fromString(status)
    }
    
    private fun mapEntityStatus(status: EntityDeviceStatus?): DeviceStatus {
        return when (status) {
            EntityDeviceStatus.ONLINE -> DeviceStatus.ONLINE
            EntityDeviceStatus.OFFLINE -> DeviceStatus.OFFLINE
            EntityDeviceStatus.CHARGING -> DeviceStatus.CHARGING
            EntityDeviceStatus.ERROR, EntityDeviceStatus.BANNED, null -> DeviceStatus.UNKNOWN
        }
    }
    
    private fun mapStatusToEntity(status: String?): EntityDeviceStatus? {
        return when (status?.lowercase()) {
            "online" -> EntityDeviceStatus.ONLINE
            "offline" -> EntityDeviceStatus.OFFLINE
            "charging" -> EntityDeviceStatus.CHARGING
            "error" -> EntityDeviceStatus.ERROR
            "banned" -> EntityDeviceStatus.BANNED
            else -> null
        }
    }
    
    private fun mapDomainStatusToEntity(status: DeviceStatus): EntityDeviceStatus? {
        return when (status) {
            DeviceStatus.ONLINE -> EntityDeviceStatus.ONLINE
            DeviceStatus.OFFLINE -> EntityDeviceStatus.OFFLINE
            DeviceStatus.CHARGING -> EntityDeviceStatus.CHARGING
            DeviceStatus.UNKNOWN -> null
        }
    }
}
