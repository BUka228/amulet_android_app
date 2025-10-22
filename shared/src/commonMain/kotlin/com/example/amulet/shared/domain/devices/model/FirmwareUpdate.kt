package com.example.amulet.shared.domain.devices.model

import kotlinx.serialization.Serializable

/**
 * Информация об обновлении прошивки.
 */
@Serializable
data class FirmwareUpdate(
    val version: String,
    val notes: String?,
    val url: String,
    val checksum: String,
    val size: Long,
    val updateAvailable: Boolean
)

/**
 * Прогресс OTA обновления.
 */
@Serializable
data class OtaUpdateProgress(
    val state: OtaUpdateState,
    val percent: Int,
    val currentBytes: Long,
    val totalBytes: Long,
    val error: String? = null
)

/**
 * Состояние OTA обновления.
 */
enum class OtaUpdateState {
    IDLE,
    PREPARING,
    DOWNLOADING,
    TRANSFERRING,
    VERIFYING,
    INSTALLING,
    COMPLETED,
    FAILED,
    CANCELLED
}
