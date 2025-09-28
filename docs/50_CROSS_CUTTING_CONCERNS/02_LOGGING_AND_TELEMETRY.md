# Логирование и телеметрия

Этот документ является единым источником правды по всем вопросам логирования и телеметрии в приложении Amulet. Он определяет стандарты, инструменты, политики и правила для обеспечения качественного мониторинга, отладки и аналитики.

## 1. Инструменты

### Логирование
- **Библиотека:** [Napier](https://github.com/AAkira/Napier) — мультиплатформенная библиотека логирования для Kotlin Multiplatform
- **Назначение:** Централизованное логирование в общем KMP-модуле (`:shared`) с выводом в нативные инструменты платформы
- **Android:** Логи выводятся в Android Logcat с соответствующими тегами и уровнями
- **Преимущества:** 
  - KMP-совместимость
  - Простота использования
  - Автоматическое форматирование
  - Поддержка различных уровней логирования

### Аналитика сбоев
- **Инструмент:** Firebase Crashlytics
- **Назначение:** Автоматический сбор и анализ крашей приложения
- **Интеграция:** Все ошибки уровня `ERROR` и `WARNING` автоматически отправляются в Crashlytics
- **Контекст:** Дополнительные ключи и метаданные для лучшего понимания причин сбоев

### Событийная телеметрия
- **Инструмент:** Firebase Analytics
- **Назначение:** Сбор пользовательских событий и поведенческой аналитики
- **Согласие:** Отправка происходит только после получения явного согласия пользователя
- **Контракт:** Все события проходят через `TelemetryRepository` с проверкой согласий

## 2. Уровни логирования

### VERBOSE (`Napier.v`)
**Назначение:** Детальная информация о жизненном цикле компонентов
**Примеры использования:**
- Методы жизненного цикла ViewModel (`onStart`, `onStop`)
- Шаги внутри UseCase
- Детали выполнения бизнес-логики
- Параметры внутренних функций

**Политика:** Включается только в debug-сборках, полностью отключается в production

```kotlin
Napier.v { "ViewModel started for screen: $screenName" }
Napier.v { "UseCase step completed: $stepName with params: $params" }
```

### DEBUG (`Napier.d`)
**Назначение:** Информация, полезная для отладки
**Примеры использования:**
- Параметры сетевых запросов (без PII)
- Состояние `State` после обновления
- Промежуточные результаты вычислений
- Детали работы алгоритмов

**Политика:** Включается только в debug-сборках, полностью отключается в production

```kotlin
Napier.d { "Network request: ${request.method} ${request.url}" }
Napier.d { "State updated: $oldState -> $newState" }
```

### INFO (`Napier.i`)
**Назначение:** Важные события в жизненном цикле приложения
**Примеры использования:**
- Пользователь залогинился/вышел
- Начало/завершение синхронизации
- BLE-устройство подключено/отключено
- Успешное выполнение критических операций
- Изменения настроек пользователя

**Политика:** Включается во всех сборках (debug и release)

```kotlin
Napier.i { "User logged in: ${user.id}" }
Napier.i { "BLE device connected: ${device.serial}" }
Napier.i { "Sync completed successfully" }
```

### WARNING (`Napier.w`)
**Назначение:** Нештатные, но не критические ситуации
**Примеры использования:**
- Сетевой запрос завершился с ошибкой, но есть retry-логика
- Получен пустой список данных, когда ожидался непустой
- Превышены лимиты времени выполнения
- Нестандартные, но обрабатываемые ошибки

**Политика:** Включается во всех сборках, отправляется в Crashlytics как non-fatal ошибка

```kotlin
Napier.w { "Network request failed, retrying: ${error.message}" }
Napier.w { "Empty data received for user: ${userId}" }
```

### ERROR (`Napier.e`)
**Назначение:** Серьезные ошибки, которые влияют на работу пользователя
**Примеры использования:**
- Необработанные исключения
- Сбой при парсинге критически важных данных
- Провал `outbox`-задачи после всех ретраев
- Критические ошибки BLE-соединения
- Ошибки аутентификации

**Политика:** Всегда включается и отправляется в Crashlytics с полным контекстом

```kotlin
Napier.e { "Critical error in BLE connection: ${error.message}", error)
Napier.e { "Failed to parse user data: ${error.message}", error)
```

## 3. Политики и правила

### Запрет персональных данных (PII)
**Строгое правило:** В логи **ЗАПРЕЩЕНО** выводить любую чувствительную информацию:

❌ **Запрещено:**
```kotlin
Napier.d { "User email: ${user.email}" }
Napier.d { "User name: ${user.displayName}" }
Napier.d { "Auth token: ${token}" }
Napier.d { "Message content: ${message.text}" }
```

✅ **Разрешено:**
```kotlin
Napier.d { "User ID: ${user.id}" }
Napier.d { "User logged in successfully" }
Napier.d { "Message sent to user: ${recipient.id}" }
```

### Логи в Production
- **VERBOSE** и **DEBUG** уровни полностью отключаются на уровне инициализации `Napier` в release-сборках
- Это предотвращает влияние на производительность и утечки информации
- Конфигурация выполняется в `Application` классе:

```kotlin
class AmuletApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        if (BuildConfig.DEBUG) {
            Napier.base(DebugAntilog())
        } else {
            Napier.base(ReleaseAntilog()) // Отключает VERBOSE и DEBUG
        }
    }
}
```

### Обязательный контекст
Логи должны быть осмысленными и содержать достаточный контекст для понимания ситуации.

❌ **Плохо:**
```kotlin
Napier.d("Error")
Napier.i("Success")
Napier.w("Failed")
```

✅ **Хорошо:**
```kotlin
Napier.d { "Failed to fetch profile for userId: $userId" }
Napier.i { "Hug sent successfully to user: $recipientId" }
Napier.w { "Network request failed for endpoint: $endpoint, retrying..." }
```

### Интеграция с Crashlytics
Все вызовы `Napier.e` и `Napier.w` должны также отправлять non-fatal ошибку в Firebase Crashlytics с дополнительными ключами:

```kotlin
// В TelemetryRepository
fun logError(level: LogLevel, message: String, throwable: Throwable? = null) {
    // Логирование через Napier
    when (level) {
        LogLevel.ERROR -> Napier.e(message, throwable)
        LogLevel.WARNING -> Napier.w(message, throwable)
        else -> { /* другие уровни */ }
    }
    
    // Отправка в Crashlytics
    if (level == LogLevel.ERROR || level == LogLevel.WARNING) {
        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("log_level", level.name)
            setCustomKey("timestamp", System.currentTimeMillis())
            setCustomKey("user_id", getCurrentUserId())
            if (throwable != null) {
                recordException(throwable)
            } else {
                log(message)
            }
        }
    }
}
```

## 4. Телеметрия

### Согласие пользователя
Отправка любой телеметрии (Firebase Analytics) производится только после получения явного согласия от пользователя через поле `User.consents`:

```kotlin
data class UserConsents(
    val analytics: Boolean = false,
    val usage: Boolean = false,
    val crash: Boolean = false,
    val diagnostics: Boolean = false
)
```

### Контракт сервиса
Вся телеметрия отправляется через `TelemetryRepository`, который внутри себя проверяет наличие согласия перед отправкой событий:

```kotlin
interface TelemetryRepository {
    suspend fun reportEvent(
        type: String,
        params: Map<String, Any> = emptyMap(),
        sessionId: String? = null,
        practiceId: String? = null
    )
    
    suspend fun reportError(
        error: AppError,
        context: Map<String, Any> = emptyMap()
    )
}

class TelemetryRepositoryImpl(
    private val userRepository: UserRepository,
    private val analytics: FirebaseAnalytics
) : TelemetryRepository {
    
    override suspend fun reportEvent(
        type: String,
        params: Map<String, Any>,
        sessionId: String?,
        practiceId: String?
    ) {
        val user = userRepository.getCurrentUser()
        if (!user.consents.analytics) {
            Napier.d { "Analytics event dropped - no consent: $type" }
            return
        }
        
        // Отправка события в Firebase Analytics
        analytics.logEvent(type, Bundle().apply {
            params.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                }
            }
        })
        
        Napier.d { "Analytics event sent: $type with params: $params" }
    }
}
```

### Типы событий телеметрии

#### Пользовательские события
```kotlin
// Вход/выход
TelemetryRepository.reportEvent("user_login")
TelemetryRepository.reportEvent("user_logout")

// Действия с устройством
TelemetryRepository.reportEvent("device_connected", mapOf(
    "device_serial" to device.serial,
    "hardware_version" to device.hardwareVersion
))

// Объятия
TelemetryRepository.reportEvent("hug_sent", mapOf(
    "recipient_id" to recipientId,
    "pattern_id" to patternId
))

// Практики
TelemetryRepository.reportEvent("practice_started", mapOf(
    "practice_id" to practiceId,
    "practice_type" to practiceType
), practiceId = practiceId)
```

#### Системные события
```kotlin
// Ошибки
TelemetryRepository.reportError(AppError.Network, mapOf(
    "endpoint" to endpoint,
    "retry_count" to retryCount
))

// Производительность
TelemetryRepository.reportEvent("performance_metric", mapOf(
    "metric_name" to "screen_load_time",
    "value_ms" to loadTime,
    "screen_name" to screenName
))
```

### Политики отправки телеметрии

#### Батчинг и очереди
- События собираются в локальную очередь (`telemetry_events` таблица)
- Отправка происходит батчами для оптимизации производительности
- При отсутствии сети события сохраняются локально и отправляются при восстановлении соединения

#### Ограничения и лимиты
- Максимальный размер батча: 100 событий
- Максимальный размер одного события: 1KB
- TTL для локальных событий: 7 дней
- При превышении лимитов старые события отбрасываются

#### Приватность и безопасность
- Все события проходят через фильтр PII
- Пользовательские ID хэшируются при необходимости
- Чувствительные данные исключаются из телеметрии
- Возможность полного отключения телеметрии через настройки

## 5. Архитектурная интеграция

### Размещение в модулях

#### `:shared` модуль
- Интерфейсы `TelemetryRepository` и `LoggingRepository`
- Доменные модели для событий телеметрии
- Утилиты для работы с логированием

#### `:core:telemetry` модуль
- Реализация `TelemetryRepositoryImpl`
- Логика батчинга и очередей
- Интеграция с Firebase Analytics и Crashlytics

#### `:data:*` модули
- Использование `TelemetryRepository` для отправки событий
- Логирование через `Napier` в критических точках
- Интеграция с `ErrorReporter` для обработки ошибок

### Интеграция с обработкой ошибок
Логирование тесно интегрировано с системой обработки ошибок из `01_ERROR_HANDLING.md`:

```kotlin
// В safeApiCall
suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T, AppError> {
    return try {
        Ok(apiCall())
    } catch (e: Exception) {
        // Логирование для отладки
        logException(e)
        
        // Отправка в телеметрию (если есть согласие)
        telemetryRepository.reportError(
            mapExceptionToAppError(e),
            mapOf("api_call" to "unknown")
        )
        
        Err(mapExceptionToAppError(e))
    }
}
```

## 6. Мониторинг и алерты

### Ключевые метрики
- **Crash-free sessions:** ≥ 99.7%
- **Время отклика API:** P95 ≤ 1.5s
- **BLE-подключения:** Успешность ≥ 95%
- **Отправка объятий:** Успешность ≥ 98%

### Алерты
- Критическое увеличение количества крашей
- Превышение лимитов времени отклика
- Массовые ошибки BLE-подключений
- Проблемы с отправкой объятий

### Дашборды
- Real-time мониторинг состояния приложения
- Аналитика пользовательского поведения
- Метрики производительности
- Отчеты по ошибкам и их частоте

## 7. Рекомендации по реализации

### Настройка Napier
```kotlin
// В Application классе
class AmuletApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setupLogging()
    }
    
    private fun setupLogging() {
        if (BuildConfig.DEBUG) {
            Napier.base(DebugAntilog())
        } else {
            Napier.base(
                object : Antilog() {
                    override fun performLog(
                        priority: LogPriority,
                        tag: String?,
                        throwable: Throwable?,
                        message: String?
                    ) {
                        // В production только INFO, WARNING, ERROR
                        if (priority >= LogPriority.INFO) {
                            // Логирование в системный лог
                            Log.println(priority.ordinal, tag ?: "Amulet", message ?: "")
                        }
                    }
                }
            )
        }
    }
}
```

### Создание утилит логирования
```kotlin
// В :shared модуле
object Logger {
    fun logUserAction(action: String, params: Map<String, Any> = emptyMap()) {
        Napier.i { "User action: $action with params: $params" }
    }
    
    fun logNetworkRequest(method: String, url: String, success: Boolean) {
        Napier.d { "Network: $method $url - ${if (success) "SUCCESS" else "FAILED"}" }
    }
    
    fun logBleEvent(event: String, deviceId: String, details: String = "") {
        Napier.i { "BLE: $event for device: $deviceId $details" }
    }
    
    fun logError(context: String, error: Throwable) {
        Napier.e { "Error in $context: ${error.message}" }
    }
}
```

### Тестирование логирования
```kotlin
// Unit тесты для проверки логирования
@Test
fun `should log user login event`() {
    // Given
    val user = User(id = "test-user")
    
    // When
    userRepository.login(user)
    
    // Then
    verify(telemetryRepository).reportEvent(
        "user_login",
        mapOf("user_id" to "test-user")
    )
}
```

## 8. Соответствие стандартам

### GDPR и приватность
- Явное согласие на телеметрию
- Возможность отзыва согласия
- Минимизация собираемых данных
- Право на удаление данных

### Безопасность
- Исключение PII из логов
- Шифрование чувствительных данных
- Безопасная передача телеметрии
- Аудит доступа к логам

### Производительность
- Асинхронная отправка телеметрии
- Батчинг событий
- Локальное кэширование
- Graceful degradation при ошибках

Этот документ обеспечивает единообразный подход к логированию и телеметрии во всем приложении Amulet, гарантируя качественный мониторинг, отладку и аналитику при соблюдении принципов приватности и безопасности.
