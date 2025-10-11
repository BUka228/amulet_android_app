package com.example.amulet.shared.core

import com.example.amulet.shared.core.AppError.BleError
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.map as resultMap
import com.github.michaelbull.result.mapBoth
import com.github.michaelbull.result.onFailure
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlin.math.min
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Выполнить действие при наличии ошибки приложения.
 *
 * @param action обработчик ошибки [AppError].
 * @return исходный [AppResult] без изменений.
 */
inline fun <T> AppResult<T>.onAppError(action: (AppError) -> Unit): AppResult<T> = onFailure(action)

/**
 * Выполнить действие только для сетевых ошибок (Network/Timeout/Server).
 *
 * @param action обработчик ошибки для сетевых сценариев.
 * @return исходный [AppResult] без изменений.
 */
inline fun <T> AppResult<T>.onNetworkError(action: (AppError) -> Unit): AppResult<T> = onFailure { error ->
    if (error is AppError.Network || error is AppError.Timeout || error is AppError.Server) {
        action(error)
    }
}

/**
 * Выполнить действие при ошибке валидации.
 *
 * @param action обработчик [AppError.Validation].
 * @return исходный [AppResult] без изменений.
 */
inline fun <T> AppResult<T>.onValidationError(action: (AppError.Validation) -> Unit): AppResult<T> =
    onFailure { error -> if (error is AppError.Validation) action(error) }

/**
 * Выполнить действие при BLE‑ошибке.
 *
 * @param action обработчик [BleError].
 * @return исходный [AppResult] без изменений.
 */
inline fun <T> AppResult<T>.onBleError(action: (BleError) -> Unit): AppResult<T> =
    onFailure { error -> if (error is BleError) action(error) }

/**
 * Трансформировать успешное значение, сохранив ошибку без изменений.
 *
 * @param transform функция преобразования успешного результата.
 * @return [Ok] с преобразованным значением или [Err] с исходной ошибкой.
 */
inline fun <T, R> AppResult<T>.mapSuccess(transform: (T) -> R): AppResult<R> = mapBoth(
    success = { Ok(transform(it)) },
    failure = { Err(it) }
)

/**
 * Вызвать действие для каждого успешного значения в потоке результатов.
 *
 * @param action suspend‑обработчик успешного значения.
 * @return исходный [Flow] для последующего чейнинга.
 */
fun <T> Flow<AppResult<T>>.onSuccess(action: suspend (T) -> Unit): Flow<AppResult<T>> =
    onEach { result ->
        result.component1()?.let { value -> action(value) }
    }

/**
 * Вызвать действие для каждой ошибки в потоке результатов.
 *
 * @param action suspend‑обработчик ошибки [AppError].
 * @return исходный [Flow] для последующего чейнинга.
 */
fun <T> Flow<AppResult<T>>.onFailure(action: suspend (AppError) -> Unit): Flow<AppResult<T>> =
    onEach { result ->
        result.component2()?.let { error -> action(error) }
    }

/**
 * Извлечь только успешные значения из потока результатов.
 *
 * @return поток значений успеха.
 */
fun <T> Flow<AppResult<T>>.successes(): Flow<T> =
    mapNotNull { it.component1() }

/**
 * Извлечь только ошибки из потока результатов.
 *
 * @return поток ошибок [AppError].
 */
fun <T> Flow<AppResult<T>>.failures(): Flow<AppError> =
    mapNotNull { it.component2() }

/**
 * Обернуть поток значений в [AppResult], маппя исключения в [AppError].
 * Исключения отмены корутин пробрасываются.
 *
 * @param mapper преобразователь исключений в [AppError].
 * @return поток результатов с [Ok] для значений и [Err] для ошибок.
 */
fun <Value> Flow<Value>.asResult(mapper: ExceptionMapper): Flow<AppResult<Value>> =
    map { value -> Ok(value) as AppResult<Value> }
        .catch { throwable ->
            if (throwable is kotlinx.coroutines.CancellationException) throw throwable
            @Suppress("UNCHECKED_CAST")
            emit(Err(mapper.mapToAppError(throwable)) as AppResult<Value>)
        }

/**
 * Повторить выполнение блока с экспоненциальной задержкой для сетевых ошибок.
 * Для не‑сетевых ошибок возвращает результат без повторов.
 *
 * @param times количество попыток (> 0).
 * @param initialDelay начальная задержка между повторами.
 * @param maxDelay максимальная задержка между повторами.
 * @param factor множитель увеличения задержки.
 * @param block выполняемый блок, возвращающий [AppResult].
 * @return первый успешный результат или последний полученный результат.
 */
suspend fun <T> retryWithBackoff(
    times: Int = 3,
    initialDelay: Duration = 1_000.milliseconds,
    maxDelay: Duration = 10_000.milliseconds,
    factor: Double = 2.0,
    block: suspend () -> AppResult<T>
): AppResult<T> {
    require(times > 0) { "times must be positive" }
    var currentDelay = initialDelay
    repeat(times - 1) {
        val result = block()
        if (result.isOk) {
            return result
        }

        val error = result.component2()
        if (error is AppError.Network || error is AppError.Timeout || error is AppError.Server) {
            delay(currentDelay)
            val nextMillis = min((currentDelay.inWholeMilliseconds * factor).toLong(), maxDelay.inWholeMilliseconds)
            currentDelay = nextMillis.milliseconds
        } else if (error != null) {
            return result
        } else {
            return result
        }
    }
    return block()
}

/**
 * Комбинировать два результата: при успехе обоих применить трансформацию,
 * при ошибке любого — вернуть ошибку.
 *
 * @param first первый результат.
 * @param second второй результат.
 * @param transform функция объединения значений.
 * @return [Ok] с объединённым значением или [Err] с ошибкой.
 */
inline fun <T1, T2, R> combineResults(
    first: AppResult<T1>,
    second: AppResult<T2>,
    crossinline transform: (T1, T2) -> R
): AppResult<R> = first.andThen { value1 ->
    second.resultMap { value2 -> transform(value1, value2) }
}
