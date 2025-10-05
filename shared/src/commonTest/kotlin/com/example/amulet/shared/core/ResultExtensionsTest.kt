package com.example.amulet.shared.core

import com.example.amulet.shared.core.AppError.Validation
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

private val testExceptionMapper = ExceptionMapper { AppError.Unknown }

class ResultExtensionsTest {

    @Test
    fun `asResult выдаёт Ok для данных и Err для исключений`() = runTest {
        val values = flow {
            emit(1)
            throw IllegalStateException("boom")
        }

        val result = values.asResult(testExceptionMapper).toList()

        assertEquals(1, result[0].component1())
        assertEquals(AppError.Unknown, result[1].component2())
    }

    @Test
    fun `retryWithBackoff повторяет при сетевых ошибках и завершается успехом`() = runTest {
        var attempt = 0

        val result = retryWithBackoff(times = 3, initialDelay = 10.milliseconds, maxDelay = 50.milliseconds) {
            attempt++
            if (attempt < 3) Err(AppError.Network) else Ok("done")
        }

        assertEquals("done", result.component1())
        assertEquals(3, attempt)
    }

    @Test
    fun `retryWithBackoff останавливается при ошибке валидации`() = runTest {
        var attempt = 0

        val result = retryWithBackoff(times = 3) {
            attempt++
            Err(Validation(emptyMap()))
        }

        assertEquals(AppError.Validation(emptyMap()), result.component2())
        assertEquals(1, attempt)
    }

    @Test
    fun `combineResults объединяет успешные значения`() {
        val combined = combineResults(Ok(2), Ok(3)) { first, second -> first + second }

        assertEquals(5, combined.component1())
    }
}
