package com.example.amulet.core.ble.model

import com.example.amulet.shared.domain.devices.model.AmuletCommand

/**
 * Результат выполнения BLE операции.
 */
sealed interface BleResult {
    data object Success : BleResult
    data class Error(val code: String, val message: String) : BleResult
}

/**
 * Прогресс загрузки анимации.
 */
data class UploadProgress(
    val totalChunks: Int,
    val sentChunks: Int,
    val state: UploadState
) {
    val percent: Int get() = if (totalChunks > 0) (sentChunks * 100) / totalChunks else 0
}

sealed interface UploadState {
    data object Preparing : UploadState
    data object Uploading : UploadState
    data object Committing : UploadState
    data object Completed : UploadState
    data class Failed(val cause: Throwable?) : UploadState
}

/**
 * Прогресс OTA обновления.
 */
data class OtaProgress(
    val totalBytes: Long,
    val sentBytes: Long,
    val state: OtaState
) {
    val percent: Int get() = if (totalBytes > 0) ((sentBytes * 100) / totalBytes).toInt() else 0
}

sealed interface OtaState {
    data object Preparing : OtaState
    data object Transferring : OtaState
    data object Verifying : OtaState
    data object Installing : OtaState
    data object Completed : OtaState
    data class Failed(val cause: Throwable?) : OtaState
}

/**
 * План анимации для загрузки на устройство.
 */
data class AnimationPlan(
    val id: String,
    val commands: List<AmuletCommand>,
    val estimatedDurationMs: Long,
    val hardwareVersion: Int
)

/**
 * Информация о прошивке для OTA.
 */
data class FirmwareInfo(
    val version: String,
    val url: String,
    val checksum: String,
    val size: Long,
    val hardwareVersion: Int
)
