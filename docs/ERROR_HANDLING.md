# Единая стратегия обработки ошибок

Цель данной стратегии — обеспечить предсказуемый, типизированный и централизованный механизм обработки ошибок на всех уровнях приложения, от источника данных до UI. Все ошибки, возникающие в `Data` слое (сеть, БД, BLE), преобразуются в единую доменную модель `AppError`, с которой работают `Domain` и `Presentation` слои.

## 1. Доменная модель ошибки (`:shared`)

Ядром системы является `sealed interface AppError`, который моделирует все возможные бизнес-ошибки в приложении.

**Важно:** Поскольку стандартный `kotlin.Result` использует `Throwable` в качестве типа ошибки, мы используем библиотеку [Result](https://github.com/michaelbull/kotlin-result) от Michael Bull, которая предоставляет `Result<Success, Failure>` с произвольными типами.

### 1.1. Зависимости

```kotlin
// В :shared/build.gradle.kts
dependencies {
    implementation("com.michael-bull.kotlin-result:kotlin-result:2.0.0")
}
```

### 1.2. Доменная модель AppError

```kotlin
// В :shared/domain/model/AppError.kt
sealed interface AppError {
    // Сетевые и системные ошибки
    data object Network : AppError // Проблемы с соединением (IOException, etc.)
    data object Timeout : AppError // Таймаут запроса
    data class Server(val code: Int, val message: String?) : AppError // 5xx ошибки
    
    // Ошибки API (4xx)
    data object Unauthorized : AppError // 401
    data object Forbidden : AppError // 403
    data object NotFound : AppError // 404
    data object Conflict : AppError // 409 (например, email уже занят)
    data object RateLimited : AppError // 429
    
    // Более специфичные бизнес-ошибки, парсятся из тела 4xx
    data class Validation(val errors: Map<String, String>) : AppError
    data class PreconditionFailed(val reason: String?) : AppError // 412 (например, "self_send" для Hugs)
    
    // Локальные ошибки
    data object DatabaseError : AppError // Ошибка при работе с Room
    data object BleError : AppError // Общая ошибка BLE
    
    // Неизвестная ошибка
    data object Unknown : AppError
}
```

### 1.3. Преимущества использования библиотеки Result

**Почему мы используем библиотеку Result от Michael Bull:**

1. **Type Safety**: `Result<T, AppError>` вместо `Result<T, Throwable>` - более строгая типизация
2. **Готовые extension-функции**: `onSuccess`, `onFailure`, `map`, `flatMap`, `combine` и многие другие
3. **KMP-совместимость**: Работает на всех платформах Kotlin Multiplatform
4. **Производительность**: Inline-функции без накладных расходов
5. **Функциональный стиль**: Чистый, декларативный код
6. **Тестируемость**: Легко мокать и тестировать
7. **Документация**: Хорошо документированная библиотека с примерами

**Встроенные функции библиотеки:**
- `onSuccess`, `onFailure` - выполнение действий
- `map`, `mapError` - трансформация значений и ошибок
- `flatMap` - цепочка операций
- `combine` - комбинирование нескольких Result
- `getOrElse`, `getOrNull` - безопасное извлечение значений
- `isSuccess`, `isFailure` - проверка состояния

## 2. Централизованный обработчик в `Data` слое

Вместо `try-catch` в каждом репозитории, мы используем централизованные обертки, которые инкапсулируют логику преобразования исключений в `AppError`.

### 2.1. Для `suspend` функций (одиночные запросы)

В модуле `:core:network` создается единая `suspend` extension-функция `safeApiCall`, которая оборачивает все вызовы к Retrofit API с детальной обработкой исключений.

```kotlin
// В :core:network/util/SafeApiCall.kt
import com.michaelbull.result.Result
import com.michaelbull.result.Ok
import com.michaelbull.result.Err

suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T, AppError> {
    return try {
        Ok(apiCall())
    } catch (e: CancellationException) {
        throw e // Пробрасываем, чтобы корутина отменилась корректно
    } catch (e: HttpException) {
        val appError = mapHttpExceptionToAppError(e)
        Err(appError)
    } catch (e: SocketTimeoutException) {
        Err(AppError.Timeout)
    } catch (e: ConnectException) {
        Err(AppError.Network)
    } catch (e: UnknownHostException) {
        Err(AppError.Network)
    } catch (e: IOException) {
        Err(AppError.Network)
    } catch (e: Exception) {
        // Логируем e для отладки
        Log.w("SafeApiCall", "Unexpected exception", e)
        Err(AppError.Unknown)
    }
}
```

**Использование в репозитории:**
```kotlin
// В UserRepositoryImpl.kt
override suspend fun getUserProfile(): Result<User, AppError> {
    return safeApiCall { apiService.getMe() }.map { it.toDomain() }
}
```

### 2.2. Для `Flow` (реактивные потоки данных)

Для потоков, особенно из Room, используется стандартный оператор `.catch`. Для сетевых потоков создается кастомный оператор, который преобразует исключения в эмиссию `Result.failure`.

```kotlin
// В :shared/domain/util/FlowExtensions.kt (KMP-friendly)
import com.michaelbull.result.Result
import com.michaelbull.result.Ok
import com.michaelbull.result.Err

fun <T> Flow<T>.asResult(): Flow<Result<T, AppError>> {
    return this
        .map<T, Result<T, AppError>> { Ok(it) }
        .catch { e ->
            val error = mapExceptionToAppError(e)
            emit(Err(error))
        }
}

// Централизованная функция маппинга исключений в AppError
fun mapExceptionToAppError(exception: Throwable): AppError {
    return when (exception) {
        // BLE-специфичные исключения (самые специфичные)
        is BleConnectionException -> AppError.BleError.DeviceNotFound
        is BleServiceDiscoveryException -> AppError.BleError.ServiceDiscoveryFailed
        is BleWriteException -> AppError.BleError.WriteFailed
        is BleReadException -> AppError.BleError.ReadFailed
        is BleTimeoutException -> AppError.BleError.CommandTimeout(exception.command)
        is BleDisconnectedException -> AppError.BleError.DeviceDisconnected
        
        // OTA-специфичные исключения
        is OtaChecksumException -> AppError.OtaError.ChecksumMismatch(exception.expected, exception.actual)
        is OtaSpaceException -> AppError.OtaError.InsufficientSpace
        is OtaInterruptedException -> AppError.OtaError.UpdateInterrupted
        is OtaCorruptedException -> AppError.OtaError.FirmwareCorrupted(exception.reason)
        
        // HTTP-исключения
        is HttpException -> mapHttpExceptionToAppError(exception)
        
        // Сетевые исключения
        is IOException -> AppError.Network
        is SocketTimeoutException -> AppError.Timeout
        is ConnectException -> AppError.Network
        is UnknownHostException -> AppError.Network
        
        // База данных
        is SQLiteException -> AppError.DatabaseError
        is RoomDatabaseException -> AppError.DatabaseError
        
        // Отмена корутин (не обрабатываем как ошибку)
        is CancellationException -> throw exception
        
        // Все остальные исключения
        else -> AppError.Unknown
    }
}

// Дополнительная функция для HTTP-исключений
fun mapHttpExceptionToAppError(httpException: HttpException): AppError {
    return when (httpException.code()) {
        401 -> AppError.Unauthorized
        403 -> AppError.Forbidden
        404 -> AppError.NotFound
        409 -> AppError.Conflict
        412 -> {
            val reason = parsePreconditionFailedReason(httpException.response()?.errorBody())
            AppError.PreconditionFailed(reason)
        }
        429 -> AppError.RateLimited
        in 400..499 -> {
            val validationErrors = parseValidationErrors(httpException.response()?.errorBody())
            AppError.Validation(validationErrors)
        }
        in 500..599 -> AppError.Server(httpException.code(), httpException.message())
        else -> AppError.Unknown
    }
}

// Вспомогательные функции для парсинга деталей ошибок
// Используем JsonElement для большей устойчивости к изменениям формата ошибки от бэкенда
private fun parsePreconditionFailedReason(errorBody: ResponseBody?): String? {
    return try {
        errorBody?.string()?.let { body ->
            val jsonElement = Json.decodeFromString<JsonElement>(body)
            // Пробуем разные варианты структуры ответа
            jsonElement.jsonObject["error"]?.jsonObject?.get("reason")?.jsonPrimitive?.contentOrNull
                ?: jsonElement.jsonObject["reason"]?.jsonPrimitive?.contentOrNull
                ?: jsonElement.jsonObject["message"]?.jsonPrimitive?.contentOrNull
                ?: jsonElement.jsonObject["error"]?.jsonPrimitive?.contentOrNull
        }
    } catch (e: Exception) {
        null
    }
}

private fun parseValidationErrors(errorBody: ResponseBody?): Map<String, String> {
    return try {
        errorBody?.string()?.let { body ->
            val jsonElement = Json.decodeFromString<JsonElement>(body)
            val errors = jsonElement.jsonObject["errors"]
            
            when {
                // Формат: { "errors": { "field1": "message1", "field2": "message2" } }
                errors is JsonObject -> {
                    errors.entries.associate { (field, value) ->
                        field to when (value) {
                            is JsonPrimitive -> value.contentOrNull ?: ""
                            is JsonArray -> value.joinToString(", ") { it.jsonPrimitive.contentOrNull ?: "" }
                            else -> ""
                        }
                    }
                }
                // Формат: { "errors": [{"field": "field1", "message": "message1"}] }
                errors is JsonArray -> {
                    errors.mapNotNull { errorElement ->
                        if (errorElement is JsonObject) {
                            val field = errorElement["field"]?.jsonPrimitive?.contentOrNull
                            val message = errorElement["message"]?.jsonPrimitive?.contentOrNull
                            if (field != null && message != null) field to message else null
                        } else null
                    }.toMap()
                }
                // Формат: { "field1": ["message1"], "field2": ["message2"] }
                else -> {
                    jsonElement.jsonObject.entries.associate { (field, value) ->
                        field to when (value) {
                            is JsonArray -> value.joinToString(", ") { it.jsonPrimitive.contentOrNull ?: "" }
                            is JsonPrimitive -> value.contentOrNull ?: ""
                            else -> ""
                        }
                    }
                }
            }
        } ?: emptyMap()
    } catch (e: Exception) {
        emptyMap()
    }
}
```

**Использование в репозитории:**
```kotlin
// В PracticesRepositoryImpl.kt
override fun observePractices(): Flow<Result<List<Practice>, AppError>> {
    return practicesDao.observeAll()
        .map { entities -> entities.map { it.toDomain() } }
        .asResult() // .catch + .map
}
```

## 3. Контракт репозиториев

Все публичные методы репозиториев в `Data` слое, которые могут завершиться ошибкой, должны возвращать либо `Result<T, AppError>`, либо `Flow<Result<T, AppError>>`. Репозиторий **никогда** не должен "пропускать" необработанные исключения (`HttpException`, `IOException` и т.д.) в `Domain` слой.

## 4. Обработка в `Domain` и `Presentation` слоях

*   **UseCase:** UseCase'ы оперируют `Result` или `Flow<Result>`, применяя бизнес-логику к успешным результатам и пробрасывая ошибки дальше.
*   **ViewModel:**
    *   Собирает `Flow<Result>` и обновляет `StateFlow<ScreenState>`.
    *   `ScreenState` содержит поле `error: AppError?`.
    *   При получении `Result.failure(error)` `ViewModel` выставляет это поле.

### 4.1. Extension-функции для Result

Библиотека Result от Michael Bull уже предоставляет большинство необходимых extension-функций. Дополнительно создаем специфичные для нашего приложения:

```kotlin
// В :shared/domain/util/ResultExtensions.kt
import com.michaelbull.result.Result
import com.michaelbull.result.Ok
import com.michaelbull.result.Err
import com.michaelbull.result.onSuccess
import com.michaelbull.result.onFailure
import com.michaelbull.result.map
import com.michaelbull.result.mapError
import com.michaelbull.result.getOrElse
import com.michaelbull.result.getOrNull
import com.michaelbull.result.isSuccess
import com.michaelbull.result.isFailure

// Дополнительные extension-функции для удобства работы с AppError
inline fun <T> Result<T, AppError>.onAppError(action: (AppError) -> Unit): Result<T, AppError> {
    return onFailure(action)
}

inline fun <T> Result<T, AppError>.onNetworkError(action: (AppError) -> Unit): Result<T, AppError> {
    return onFailure { error ->
        if (error is AppError.Network || error is AppError.Timeout) {
            action(error)
        }
    }
}

inline fun <T> Result<T, AppError>.onValidationError(action: (AppError.Validation) -> Unit): Result<T, AppError> {
    return onFailure { error ->
        if (error is AppError.Validation) {
            action(error)
        }
    }
}

// Для Flow<Result> - используем встроенные функции библиотеки
inline fun <T> Flow<Result<T, AppError>>.onSuccess(action: suspend (T) -> Unit): Flow<Result<T, AppError>> {
    return this.onEach { result ->
        result.onSuccess(action)
    }
}

inline fun <T> Flow<Result<T, AppError>>.onFailure(action: suspend (AppError) -> Unit): Flow<Result<T, AppError>> {
    return this.onEach { result ->
        result.onFailure(action)
    }
}

// Специфичные для Amulet App функции
inline fun <T> Flow<Result<T, AppError>>.onBleError(action: suspend (AppError.BleError) -> Unit): Flow<Result<T, AppError>> {
    return this.onEach { result ->
        result.onFailure { error ->
            if (error is AppError.BleError) {
                action(error)
            }
        }
    }
}

inline fun <T> Flow<Result<T, AppError>>.onHugsError(action: suspend (AppError) -> Unit): Flow<Result<T, AppError>> {
    return this.onEach { result ->
        result.onFailure { error ->
            when (error) {
                is AppError.PreconditionFailed -> action(error)
                is AppError.RateLimited -> action(error)
                else -> { /* игнорируем другие ошибки */ }
            }
        }
    }
}
```

### 4.2. Упрощенный код в ViewModel

С использованием extension-функций код в ViewModel становится более лаконичным:

```kotlin
// В ProfileViewModel.kt
import com.michaelbull.result.Result
import com.michaelbull.result.Ok
import com.michaelbull.result.Err

private val _uiState = MutableStateFlow(ProfileState(isLoading = true))
val uiState: StateFlow<ProfileState> = _uiState.asStateFlow()

init {
    viewModelScope.launch {
        getUserProfileUseCase()
            .map { result ->
                when (result) {
                    is Ok -> ProfileState(
                        isLoading = false, 
                        user = result.value, 
                        error = null
                    )
                    is Err -> ProfileState(
                        isLoading = false, 
                        user = null, 
                        error = result.error
                    )
                }
            }
            .collect { newState ->
                _uiState.value = newState
            }
    }
}
```

### 4.3. Дополнительные утилиты для ViewModel

```kotlin
// В :shared/domain/util/ViewModelExtensions.kt
import com.michaelbull.result.Result
import com.michaelbull.result.Ok
import com.michaelbull.result.Err

// Специфичные для Amulet App функции
inline fun <T> Flow<Result<T, AppError>>.onBleError(action: suspend (AppError.BleError) -> Unit): Flow<Result<T, AppError>> {
    return this.onEach { result ->
        result.onFailure { error ->
            if (error is AppError.BleError) {
                action(error)
            }
        }
    }
}

inline fun <T> Flow<Result<T, AppError>>.onHugsError(action: suspend (AppError) -> Unit): Flow<Result<T, AppError>> {
    return this.onEach { result ->
        result.onFailure { error ->
            when (error) {
                is AppError.PreconditionFailed -> action(error)
                is AppError.RateLimited -> action(error)
                else -> { /* игнорируем другие ошибки */ }
            }
        }
    }
}

// Утилита для комбинирования нескольких Result
inline fun <T1, T2, R> combineResults(
    result1: Result<T1, AppError>,
    result2: Result<T2, AppError>,
    transform: (T1, T2) -> R
): Result<R, AppError> {
    return combine(result1, result2, transform)
}
```

### 4.4. Дополнительные extension-функции

```kotlin
// В :shared/domain/util/ResultExtensions.kt
import com.michaelbull.result.Result
import com.michaelbull.result.Ok
import com.michaelbull.result.Err

// Фильтрация успешных результатов
fun <T> Flow<Result<T, AppError>>.successes(): Flow<T> {
    return this
        .filter { it is Ok }
        .map { (it as Ok).value }
}

// Фильтрация ошибок
fun <T> Flow<Result<T, AppError>>.failures(): Flow<AppError> {
    return this
        .filter { it is Err }
        .map { (it as Err).error }
}

// Retry с экспоненциальным backoff
suspend fun <T> retryWithBackoff(
    times: Int = 3,
    initialDelay: Long = 1000,
    maxDelay: Long = 10000,
    factor: Double = 2.0,
    block: suspend () -> Result<T, AppError>
): Result<T, AppError> {
    var currentDelay = initialDelay
    repeat(times - 1) { attempt ->
        when (val result = block()) {
            is Ok -> return result
            is Err -> {
                if (result.error is AppError.Network || result.error is AppError.Timeout) {
                    delay(currentDelay)
                    currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
                } else {
                    return result // Не ретраим для других типов ошибок
                }
            }
        }
    }
    return block() // Последняя попытка
}
```

## 5. Отображение ошибок в UI

*   UI (Compose) подписывается на `uiState.error` и реагирует на его изменение.
*   Создается единый `Composable` `ErrorHandler` или `ErrorView`, который принимает `AppError` и решает, что показать: `Snackbar`, `Toast`, полноэкранное сообщение об ошибке с кнопкой "Повторить" и т.д.

```kotlin
// В ProfileScreen.kt
val state by viewModel.uiState.collectAsState()

if (state.error != null) {
    FullScreenError(
        error = state.error,
        onRetry = { viewModel.onRetryClicked() }
    )
} else if (state.isLoading) {
    // ...
} else {
    // ...
}
```

## 6. Специфичные ошибки для Amulet App

### 6.1. BLE-специфичные ошибки

```kotlin
sealed interface BleError : AppError {
    data object DeviceNotFound : BleError
    data object ConnectionFailed : BleError
    data object ServiceDiscoveryFailed : BleError
    data object WriteFailed : BleError
    data object ReadFailed : BleError
    data class CommandTimeout(val command: String) : BleError
    data object DeviceDisconnected : BleError
}

// BLE-специфичные исключения
class BleConnectionException(message: String) : Exception(message)
class BleServiceDiscoveryException(message: String) : Exception(message)
class BleWriteException(message: String) : Exception(message)
class BleReadException(message: String) : Exception(message)
class BleTimeoutException(val command: String, message: String) : Exception(message)
class BleDisconnectedException(message: String) : Exception(message)
```

### 6.2. OTA-специфичные ошибки

```kotlin
sealed interface OtaError : AppError {
    data object NoUpdateAvailable : OtaError
    data class ChecksumMismatch(val expected: String, val actual: String) : OtaError
    data object InsufficientSpace : OtaError
    data object UpdateInterrupted : OtaError
    data class FirmwareCorrupted(val reason: String) : OtaError
}

// OTA-специфичные исключения
class OtaChecksumException(val expected: String, val actual: String) : Exception("Checksum mismatch: expected $expected, got $actual")
class OtaSpaceException(message: String) : Exception(message)
class OtaInterruptedException(message: String) : Exception(message)
class OtaCorruptedException(val reason: String) : Exception("Firmware corrupted: $reason")
```

### 6.3. Hugs-специфичные ошибки

```kotlin
sealed interface HugsError : AppError {
    data object SelfSend : HugsError // Попытка отправить объятие самому себе
    data object PairBlocked : HugsError // Пара заблокирована
    data object RateLimited : HugsError // Превышен лимит отправки объятий
    data object RecipientNotFound : HugsError // Получатель не найден
}
```

## 7. Обработка ошибок в очереди исходящих действий

Для `outbox_actions` используется специальная обработка ошибок:

```kotlin
// В :core:database/OutboxActionEntity.kt
data class OutboxActionEntity(
    val id: String,
    val type: String,
    val payloadJson: String,
    val status: String, // PENDING, IN_FLIGHT, FAILED, COMPLETED
    val retryCount: Int,
    val maxRetries: Int,
    val lastError: String?, // Сохранение AppError.toString()
    val idempotencyKey: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val availableAt: Long,
    val priority: Int
)

// В :data:outbox/OutboxRepository.kt
suspend fun processAction(action: OutboxActionEntity): Result<Unit, AppError> {
    return try {
        when (action.type) {
            "HUG_SEND" -> processHugSend(action)
            "DEVICE_CLAIM" -> processDeviceClaim(action)
            // ... другие типы
            else -> Result.failure(AppError.Unknown)
        }
    } catch (e: Exception) {
        val appError = mapExceptionToAppError(e)
        Result.failure(appError)
    }
}
```

## 8. Логирование и телеметрия

Все ошибки должны логироваться для отладки и отправляться в телеметрию:

```kotlin
// В :core:telemetry/ErrorReporter.kt
class ErrorReporter {
    fun reportError(error: AppError, context: Map<String, Any> = emptyMap()) {
        // Логирование для разработчиков
        Log.e("AppError", "Error occurred: $error", context)
        
        // Отправка в телеметрию (с учетом согласий пользователя)
        if (userConsents.analytics) {
            telemetryRepository.reportEvent(
                type = "error_occurred",
                params = mapOf(
                    "error_type" to error::class.simpleName,
                    "error_details" to error.toString(),
                    "context" to context
                )
            )
        }
    }
}
```

## 9. Тестирование обработки ошибок

```kotlin
// В :shared/domain/error/AppErrorTest.kt
class AppErrorTest {
    @Test
    fun `should map HttpException to correct AppError`() {
        val httpException = HttpException(Response.error(404, "Not Found".toResponseBody()))
        val result = mapHttpExceptionToAppError(httpException)
        
        assertThat(result).isInstanceOf(AppError.NotFound::class.java)
    }
    
    @Test
    fun `should map IOException to Network error`() {
        val ioException = IOException("Network error")
        val result = mapExceptionToAppError(ioException)
        
        assertThat(result).isInstanceOf(AppError.Network::class.java)
    }
}
```

## 10. Рекомендации по реализации

1. **Всегда используйте `Result<T, AppError>`** для операций, которые могут завершиться ошибкой
2. **Не пробрасывайте исключения** из Data слоя в Domain/Presentation
3. **Используйте `JsonElement` для парсинга ошибок** - это обеспечивает устойчивость к изменениям формата от бэкенда
4. **Логируйте все ошибки** для отладки, но не в продакшене
5. **Учитывайте согласия пользователя** при отправке телеметрии
6. **Предоставляйте понятные сообщения** пользователю на основе `AppError`
7. **Тестируйте обработку ошибок** в unit-тестах
8. **Используйте retry-логику** для временных ошибок (сеть, BLE)
9. **Не показывайте технические детали** ошибок пользователю

### 10.1. Использование JsonElement для парсинга ошибок

Для парсинга ошибок от бэкенда рекомендуется использовать `JsonElement` вместо строго типизированных DTO:

**Преимущества:**
- **Устойчивость к изменениям** - бэкенд может изменить структуру ошибки без поломки клиента
- **Гибкость** - можно обрабатывать разные форматы ошибок в одном коде
- **Отказоустойчивость** - отсутствие поля не приводит к краху парсинга

**Пример использования:**
```kotlin
// Вместо строгой типизации
@Serializable
data class ErrorResponse(val message: String)

// Используем JsonElement
val jsonElement = Json.decodeFromString<JsonElement>(body)
val message = jsonElement.jsonObject["message"]?.jsonPrimitive?.contentOrNull
    ?: jsonElement.jsonObject["error"]?.jsonPrimitive?.contentOrNull
    ?: "Unknown error"
```

**Поддерживаемые форматы ошибок:**
- `{"message": "Error text"}`
- `{"error": {"reason": "self_send"}}`
- `{"errors": {"field1": "message1", "field2": "message2"}}`
- `{"errors": [{"field": "field1", "message": "message1"}]}`
- `{"field1": ["message1"], "field2": ["message2"]}`

### 10.2. Иерархия исключений в catch-блоках

При обработке исключений в `catch`-блоках важно соблюдать правильную иерархию - от самых специфичных к самым общим:

```kotlin
.catch { e ->
    val error = when (e) {
        // 1. Самые специфичные исключения (конкретные типы)
        is BleConnectionException -> AppError.BleError.DeviceNotFound
        is BleTimeoutException -> AppError.BleError.CommandTimeout(e.command)
        is OtaChecksumException -> AppError.OtaError.ChecksumMismatch(e.expected, e.actual)
        
        // 2. Специфичные исключения по доменам
        is BleException -> AppError.BleError.ConnectionFailed
        is OtaException -> AppError.OtaError.UpdateInterrupted
        
        // 3. HTTP-исключения
        is HttpException -> mapHttpExceptionToAppError(e)
        
        // 4. Сетевые исключения (более общие)
        is SocketTimeoutException -> AppError.Timeout
        is ConnectException -> AppError.Network
        is UnknownHostException -> AppError.Network
        is IOException -> AppError.Network
        
        // 5. Исключения базы данных
        is SQLiteException -> AppError.DatabaseError
        is RoomDatabaseException -> AppError.DatabaseError
        
        // 6. Отмена корутин (не обрабатываем как ошибку)
        is CancellationException -> throw e
        
        // 7. Все остальные (самые общие)
        else -> AppError.Unknown
    }
    emit(Result.failure(error))
}
```

**Правила иерархии:**
- **Специфичные исключения** должны быть обработаны первыми
- **Наследники** должны быть обработаны раньше родительских классов
- **CancellationException** всегда пробрасывается (не обрабатывается как ошибка)
- **Общие исключения** (Exception, Throwable) должны быть в конце

## 11. Примеры использования в разных сценариях

### Отправка объятия
```kotlin
// В :feature:hugs/SendHugUseCase.kt
class SendHugUseCase {
    suspend fun execute(request: SendHugRequest): Result<Hug, AppError> {
        return hugsRepository.sendHug(request)
            .onFailure { error ->
                when (error) {
                    is HugsError.SelfSend -> {
                        // Показать специфичное сообщение
                    }
                    is HugsError.PairBlocked -> {
                        // Предложить разблокировать пару
                    }
                    is AppError.RateLimited -> {
                        // Показать время до следующей отправки
                    }
                }
            }
    }
}
```

### BLE подключение
```kotlin
// В :core:ble/AmuletBleClient.kt
class AmuletBleClient {
    fun observeConnectionState(): Flow<Result<ConnectionState, AppError>> {
        return connectionStateFlow
            .map { state ->
                when (state) {
                    is ConnectionState.Connected -> Result.success(state)
                    is ConnectionState.Failed -> Result.failure(mapBleErrorToAppError(state.cause))
                    else -> Result.success(state)
                }
            }
    }
}
```

Эта стратегия обеспечивает единообразную, предсказуемую и тестируемую обработку ошибок во всем приложении Amulet.
