package com.example.amulet.shared.core

import com.github.michaelbull.result.Result

sealed interface AppError {
    // Network / infrastructure
    data object Network : AppError
    data object Timeout : AppError
    data class Server(val code: Int, val message: String?) : AppError

    // HTTP client errors
    data object Unauthorized : AppError
    data object Forbidden : AppError
    data object NotFound : AppError
    data object Conflict : AppError
    data class VersionConflict(val serverVersion: Int?) : AppError
    data object RateLimited : AppError
    data class Validation(val errors: Map<String, String>) : AppError
    data class PreconditionFailed(val reason: String?) : AppError

    // Local storage / infra
    data object DatabaseError : AppError

    // BLE
    sealed interface BleError : AppError {
        data object DeviceNotFound : BleError
        data object ConnectionFailed : BleError
        data object ServiceDiscoveryFailed : BleError
        data object WriteFailed : BleError
        data object ReadFailed : BleError
        data class CommandTimeout(val command: String) : BleError
        data object DeviceDisconnected : BleError
    }

    // OTA
    sealed interface OtaError : AppError {
        data object NoUpdateAvailable : OtaError
        data class ChecksumMismatch(val expected: String, val actual: String) : OtaError
        data object InsufficientSpace : OtaError
        data object UpdateInterrupted : OtaError
        data class FirmwareCorrupted(val reason: String) : OtaError
    }

    data object Unknown : AppError
}

typealias AppResult<T> = Result<T, AppError>
