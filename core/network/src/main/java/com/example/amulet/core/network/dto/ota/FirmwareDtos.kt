package com.example.amulet.core.network.dto.ota

import com.example.amulet.core.network.serialization.ApiTimestamp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO для информации о прошивке.
 * Соответствует OpenAPI схеме `FirmwareInfo`.
 */
@Serializable
data class FirmwareInfoDto(
    @SerialName("version")
    val version: String,
    
    @SerialName("notes")
    val notes: String? = null,
    
    @SerialName("url")
    val url: String,
    
    @SerialName("checksum")
    val checksum: String,
    
    @SerialName("size")
    val size: Long,
    
    @SerialName("updateAvailable")
    val updateAvailable: Boolean = true
)

/**
 * DTO для отчета об установке прошивки.
 * Соответствует OpenAPI схеме `FirmwareReportRequest`.
 */
@Serializable
data class FirmwareReportRequestDto(
    @SerialName("fromVersion")
    val fromVersion: String,
    
    @SerialName("toVersion")
    val toVersion: String,
    
    @SerialName("status")
    val status: String, // "success" | "failed" | "cancelled"
    
    @SerialName("errorCode")
    val errorCode: String? = null,
    
    @SerialName("errorMessage")
    val errorMessage: String? = null,
    
    @SerialName("metadata")
    val metadata: Map<String, String>? = null
)

/**
 * DTO для расширенной информации о прошивке (админ).
 * Соответствует OpenAPI схеме `AdminFirmwareInfo`.
 */
@Serializable
data class AdminFirmwareInfoDto(
    @SerialName("id")
    val id: String,
    
    @SerialName("version")
    val version: String,
    
    @SerialName("hardwareVersion")
    val hardwareVersion: Int,
    
    @SerialName("notes")
    val notes: String? = null,
    
    @SerialName("url")
    val url: String? = null,
    
    @SerialName("checksum")
    val checksum: String? = null,
    
    @SerialName("size")
    val size: Long? = null,
    
    @SerialName("isPublished")
    val isPublished: Boolean,
    
    @SerialName("rolloutStage")
    val rolloutStage: String? = null,
    
    @SerialName("metadata")
    val metadata: Map<String, String>? = null,
    
    @SerialName("createdAt")
    val createdAt: ApiTimestamp,
    
    @SerialName("updatedAt")
    val updatedAt: ApiTimestamp
)

/**
 * Обертка для ответа с информацией о прошивке.
 */
@Serializable
data class FirmwareInfoResponseDto(
    @SerialName("firmware")
    val firmware: FirmwareInfoDto
)

/**
 * Обертка для успешного ответа.
 */
@Serializable
data class OkResponseDto(
    @SerialName("ok")
    val ok: Boolean
)
