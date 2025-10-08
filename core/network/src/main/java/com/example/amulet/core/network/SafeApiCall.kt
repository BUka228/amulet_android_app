package com.example.amulet.core.network

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.ExceptionMapper
import com.example.amulet.shared.core.toAppError
import com.example.amulet.shared.core.logging.Logger
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kotlinx.coroutines.CancellationException

private const val TAG = "SafeApiCall"

suspend fun <T> safeApiCall(
    mapper: ExceptionMapper,
    logError: (Throwable) -> Unit = { throwable ->
        Logger.w(
            message = throwable.message ?: throwable::class.java.simpleName,
            throwable = throwable,
            tag = TAG
        )
    },
    block: suspend () -> T
): AppResult<T> = try {
    Ok(block())
} catch (cancellationException: CancellationException) {
    throw cancellationException
} catch (throwable: Throwable) {
    logError(throwable)
    Err(throwable.toAppError(mapper))
}
