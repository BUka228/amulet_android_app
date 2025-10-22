package com.example.amulet.data.devices.mapper

import com.example.amulet.core.database.entity.FirmwareInfoEntity
import com.example.amulet.core.network.dto.ota.FirmwareInfoDto
import com.example.amulet.shared.domain.devices.model.FirmwareUpdate
import javax.inject.Inject

/**
 * Маппер для преобразования данных о прошивках между слоями.
 */
class OtaMapper @Inject constructor() {
    
    /**
     * DTO -> Domain
     */
    fun toDomain(dto: FirmwareInfoDto): FirmwareUpdate {
        return FirmwareUpdate(
            version = dto.version,
            notes = dto.notes,
            url = dto.url,
            checksum = dto.checksum,
            size = dto.size,
            updateAvailable = dto.updateAvailable
        )
    }
    
    /**
     * Entity -> Domain
     */
    fun toDomain(entity: FirmwareInfoEntity): FirmwareUpdate {
        return FirmwareUpdate(
            version = entity.versionName,
            notes = entity.changelog,
            url = entity.downloadUrl,
            checksum = "", // Checksum не хранится в Entity, будет получен из DTO
            size = 0L, // Size не хранится в Entity
            updateAvailable = true
        )
    }
    
    /**
     * DTO -> Entity
     */
    fun toEntity(dto: FirmwareInfoDto, hardwareVersion: Int): FirmwareInfoEntity {
        return FirmwareInfoEntity(
            id = "${hardwareVersion}_${dto.version}",
            hardwareVersion = hardwareVersion,
            versionName = dto.version,
            versionCode = parseVersionCode(dto.version),
            downloadUrl = dto.url,
            changelog = dto.notes,
            mandatory = false, // По умолчанию необязательное
            cachedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Парсинг версии в числовой код для сравнения.
     * Например: "2.1.0" -> 2010000
     */
    private fun parseVersionCode(version: String): Int {
        val parts = version.split(".")
        if (parts.size != 3) return 0
        
        return try {
            val major = parts[0].toInt()
            val minor = parts[1].toInt()
            val patch = parts[2].toInt()
            major * 1_000_000 + minor * 1_000 + patch
        } catch (e: NumberFormatException) {
            0
        }
    }
}
