package com.example.amulet.core.sync.internal

import com.example.amulet.core.database.entity.OutboxActionEntity
import com.example.amulet.shared.core.AppError
import javax.inject.Inject

class ActionErrorResolver @Inject constructor(
    private val backoffPolicy: BackoffPolicy
) {

    fun resolve(action: OutboxActionEntity, error: AppError): ActionResolution =
        when (error) {
            AppError.Network,
            AppError.Timeout,
            is AppError.Server,
            AppError.RateLimited,
            AppError.DatabaseError -> retry(action, error)

            is AppError.BleError,
            is AppError.OtaError,
            AppError.Unauthorized,
            AppError.Forbidden,
            AppError.NotFound,
            AppError.Conflict,
            is AppError.VersionConflict,
            is AppError.Validation,
            is AppError.PreconditionFailed,
            AppError.Unknown -> ActionResolution.Failed(error.describe())
        }

    private fun retry(action: OutboxActionEntity, error: AppError): ActionResolution {
        val delayMillis = backoffPolicy.nextDelayMillis(action.retryCount)
        return ActionResolution.Retry(delayMillis, error.describe())
    }

    private fun AppError.describe(): String = when (this) {
        is AppError.Server -> "Server(${this.code}): ${this.message ?: "no message"}"
        is AppError.Validation -> this.errors.entries.joinToString(
            prefix = "Validation[",
            postfix = "]"
        ) { (field, message) -> "$field=$message" }
        is AppError.VersionConflict -> "VersionConflict(serverVersion=${this.serverVersion})"
        is AppError.PreconditionFailed -> "PreconditionFailed(${this.reason ?: "unknown"})"
        is AppError.BleError -> "BleError(${this::class.simpleName})"
        is AppError.OtaError -> "OtaError(${this::class.simpleName})"
        AppError.Network -> "Network"
        AppError.Timeout -> "Timeout"
        AppError.Unauthorized -> "Unauthorized"
        AppError.Forbidden -> "Forbidden"
        AppError.NotFound -> "NotFound"
        AppError.Conflict -> "Conflict"
        AppError.RateLimited -> "RateLimited"
        AppError.DatabaseError -> "DatabaseError"
        AppError.Unknown -> "Unknown"
    }
}
