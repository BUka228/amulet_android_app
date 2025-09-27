# Единая стратегия обработки ошибок

Цель данной стратегии — обеспечить предсказуемый, типизированный и централизованный механизм обработки ошибок на всех уровнях приложения, от источника данных до UI. Все ошибки, возникающие в `Data` слое (сеть, БД, BLE), преобразуются в единую доменную модель `AppError`, с которой работают `Domain` и `Presentation` слои.

## 1. Доменная модель ошибки (`:shared`)

Ядром системы является `sealed interface AppError`, который моделирует все возможные бизнес-ошибки в приложении.

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

## 2. Централизованный обработчик в `Data` слое

Вместо `try-catch` в каждом репозитории, мы используем централизованные обертки, которые инкапсулируют логику преобразования исключений в `AppError`.

### 2.1. Для `suspend` функций (одиночные запросы)

В модуле `:core:network` создается `suspend` extension-функция, которая оборачивает все вызовы к Retrofit API.

```kotlin
// В :core:network/util/SafeApiCall.kt
suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T, AppError> {
    return try {
        Result.success(apiCall())
    } catch (e: CancellationException) {
        throw e // Пробрасываем, чтобы корутина отменилась корректно
    } catch (e: HttpException) {
        val appError = when (e.code()) {
            401 -> AppError.Unauthorized
            403 -> AppError.Forbidden
            404 -> AppError.NotFound
            409 -> AppError.Conflict
            429 -> AppError.RateLimited
            412 -> {
                // Пример парсинга деталей ошибки
                val reason = parsePreconditionFailedReason(e.response()?.errorBody())
                AppError.PreconditionFailed(reason)
            }
            in 400..499 -> AppError.Validation(/* parse details */)
            else -> AppError.Server(e.code(), e.message())
        }
        Result.failure(appError)
    } catch (e: IOException) {
        Result.failure(AppError.Network)
    } catch (e: Exception) {
        // Логируем e для отладки
        Result.failure(AppError.Unknown)
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
fun <T> Flow<T>.asResult(): Flow<Result<T, AppError>> {
    return this
        .map<T, Result<T, AppError>> { Result.success(it) }
        .catch { e ->
            // Здесь можно добавить более сложную логику маппинга, если нужно
            val error = when(e) {
                is IOException -> AppError.Network
                is SQLiteException -> AppError.DatabaseError
                // ... другие типы
                else -> AppError.Unknown
            }
            emit(Result.failure(error))
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

    ```kotlin
    // В ProfileViewModel.kt
    private val _uiState = MutableStateFlow(ProfileState(isLoading = true))
    val uiState: StateFlow<ProfileState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getUserProfileUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update { it.copy(isLoading = false, user = result.value, error = null) }
                    }
                    is Result.Failure -> {
                        _uiState.update { it.copy(isLoading = false, error = result.error) }
                    }
                }
            }
        }
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
3. **Логируйте все ошибки** для отладки, но не в продакшене
4. **Учитывайте согласия пользователя** при отправке телеметрии
5. **Предоставляйте понятные сообщения** пользователю на основе `AppError`
6. **Тестируйте обработку ошибок** в unit-тестах
7. **Используйте retry-логику** для временных ошибок (сеть, BLE)
8. **Не показывайте технические детали** ошибок пользователю

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
