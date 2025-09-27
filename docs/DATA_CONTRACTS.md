# Enterprise Data Layer Contracts

## Обзор

Данный документ определяет контракты и интерфейсы для enterprise-уровня data слоя приложения Amulet. Контракты обеспечивают надежность, масштабируемость, безопасность и соответствие корпоративным стандартам.

## Архитектурные принципы

### 1. Clean Architecture с Enterprise-требованиями

- **Строгая изоляция слоев**: Data слой полностью изолирован от Presentation и Domain слоев
- **Инверсия зависимостей**: Все зависимости направлены внутрь, к Domain слою
- **Единая обработка ошибок**: Централизованная система обработки ошибок с типизированными результатами
- **Offline-first подход**: Локальная база данных как источник истины
- **Реактивность**: Все операции возвращают Flow для реактивного программирования

### 2. Enterprise-требования

- **Надежность**: Гарантированная доставка операций через outbox-паттерн
- **Безопасность**: Полное шифрование данных, безопасное хранение ключей
- **Масштабируемость**: Поддержка пагинации, кэширования, оптимизации запросов
- **Аудит**: Полное логирование всех операций для соответствия требованиям
- **Производительность**: Оптимизированные запросы, индексы, стратегии кэширования

## Контракты репозиториев

### 1. Базовый контракт репозитория

```kotlin
// :shared/domain/repository/BaseRepository.kt
interface BaseRepository<T, ID> {
    suspend fun getById(id: ID): Result<T, AppError>
    suspend fun getAll(): Flow<Result<List<T>, AppError>>
    suspend fun save(entity: T): Result<T, AppError>
    suspend fun delete(id: ID): Result<Unit, AppError>
    suspend fun exists(id: ID): Result<Boolean, AppError>
}
```

### 2. Контракт с пагинацией

```kotlin
// :shared/domain/repository/PaginatedRepository.kt
interface PaginatedRepository<T> {
    fun getPaged(
        cursor: String? = null,
        limit: Int = 20,
        filters: Map<String, Any> = emptyMap()
    ): Flow<Result<PaginatedResult<T>, AppError>>
    
    suspend fun refresh(): Result<Unit, AppError>
    suspend fun invalidateCache(): Result<Unit, AppError>
}

data class PaginatedResult<T>(
    val items: List<T>,
    val nextCursor: String?,
    val hasMore: Boolean,
    val totalCount: Int? = null
)
```

### 3. Контракт с кэшированием

```kotlin
// :shared/domain/repository/CachedRepository.kt
interface CachedRepository<T, ID> : BaseRepository<T, ID> {
    suspend fun getCached(id: ID): Result<T?, AppError>
    suspend fun invalidateCache(id: ID): Result<Unit, AppError>
    suspend fun preload(ids: List<ID>): Result<Unit, AppError>
    fun observeCacheStatus(): Flow<CacheStatus>
}

data class CacheStatus(
    val hitRate: Double,
    val size: Int,
    val maxSize: Int,
    val lastCleanup: Long
)
```

## Специализированные контракты репозиториев

### 1. UserRepository

```kotlin
// :shared/domain/repository/UserRepository.kt
interface UserRepository : CachedRepository<User, String> {
    suspend fun getCurrentUser(): Result<User, AppError>
    suspend fun updateProfile(updates: UserUpdateRequest): Result<User, AppError>
    suspend fun updateConsents(consents: Map<String, Boolean>): Result<User, AppError>
    suspend fun deleteAccount(): Result<Unit, AppError>
    
    fun observeUserChanges(): Flow<Result<User, AppError>>
    suspend fun syncWithServer(): Result<Unit, AppError>
}
```

### 2. DevicesRepository

```kotlin
// :shared/domain/repository/DevicesRepository.kt
interface DevicesRepository : PaginatedRepository<Device> {
    suspend fun claimDevice(serial: String, claimToken: String): Result<Device, AppError>
    suspend fun updateDeviceSettings(deviceId: String, settings: DeviceSettings): Result<Device, AppError>
    suspend fun unclaimDevice(deviceId: String): Result<Unit, AppError>
    suspend fun getDeviceStatus(deviceId: String): Result<DeviceStatus, AppError>
    
    fun observeDeviceConnection(deviceId: String): Flow<Result<ConnectionState, AppError>>
    fun observeBatteryLevel(deviceId: String): Flow<Result<Int, AppError>>
    suspend fun sendCommand(deviceId: String, command: DeviceCommand): Result<Unit, AppError>
}
```

### 3. HugsRepository

```kotlin
// :shared/domain/repository/HugsRepository.kt
interface HugsRepository : PaginatedRepository<Hug> {
    suspend fun sendHug(request: SendHugRequest): Result<Hug, AppError>
    suspend fun getHugById(hugId: String): Result<Hug, AppError>
    suspend fun markAsDelivered(hugId: String): Result<Unit, AppError>
    
    fun observeSentHugs(): Flow<Result<List<Hug>, AppError>>
    fun observeReceivedHugs(): Flow<Result<List<Hug>, AppError>>
    fun observeHugDelivery(hugId: String): Flow<Result<DeliveryStatus, AppError>>
}
```

### 4. PatternsRepository

```kotlin
// :shared/domain/repository/PatternsRepository.kt
interface PatternsRepository : PaginatedRepository<Pattern> {
    suspend fun createPattern(request: CreatePatternRequest): Result<Pattern, AppError>
    suspend fun updatePattern(id: String, request: UpdatePatternRequest): Result<Pattern, AppError>
    suspend fun deletePattern(id: String): Result<Unit, AppError>
    suspend fun sharePattern(id: String, request: SharePatternRequest): Result<Unit, AppError>
    suspend fun previewPattern(deviceId: String, spec: PatternSpec): Result<Unit, AppError>
    
    fun observeMyPatterns(): Flow<Result<List<Pattern>, AppError>>
    fun observePublicPatterns(): Flow<Result<List<Pattern>, AppError>>
    suspend fun searchPatterns(query: String, filters: PatternFilters): Result<List<Pattern>, AppError>
}
```

### 5. PracticesRepository

```kotlin
// :shared/domain/repository/PracticesRepository.kt
interface PracticesRepository : PaginatedRepository<Practice> {
    suspend fun getPracticeById(id: String): Result<Practice, AppError>
    suspend fun startSession(practiceId: String, request: StartSessionRequest): Result<Session, AppError>
    suspend fun stopSession(sessionId: String, request: StopSessionRequest): Result<SessionSummary, AppError>
    
    fun observeActiveSessions(): Flow<Result<List<Session>, AppError>>
    fun observeSessionProgress(sessionId: String): Flow<Result<SessionProgress, AppError>>
    suspend fun getPracticeStats(practiceId: String): Result<PracticeStats, AppError>
}
```

### 6. RulesRepository

```kotlin
// :shared/domain/repository/RulesRepository.kt
interface RulesRepository : BaseRepository<Rule, String> {
    suspend fun createRule(request: CreateRuleRequest): Result<Rule, AppError>
    suspend fun updateRule(id: String, request: UpdateRuleRequest): Result<Rule, AppError>
    suspend fun enableRule(id: String): Result<Unit, AppError>
    suspend fun disableRule(id: String): Result<Unit, AppError>
    suspend fun testRule(id: String): Result<RuleTestResult, AppError>
    
    fun observeActiveRules(): Flow<Result<List<Rule>, AppError>>
    suspend fun evaluateLocalTriggers(event: LocalEvent): Result<List<RuleAction>, AppError>
}
```

### 7. TelemetryRepository

```kotlin
// :shared/domain/repository/TelemetryRepository.kt
interface TelemetryRepository {
    suspend fun reportEvent(event: TelemetryEvent): Result<Unit, AppError>
    suspend fun reportBatch(events: List<TelemetryEvent>): Result<Unit, AppError>
    suspend fun flushPendingEvents(): Result<Int, AppError>
    
    fun observeEventQueue(): Flow<Result<QueueStatus, AppError>>
    suspend fun getEventHistory(limit: Int = 100): Result<List<TelemetryEvent>, AppError>
}
```

## Контракты для работы с очередями

### 1. OutboxRepository

```kotlin
// :shared/domain/repository/OutboxRepository.kt
interface OutboxRepository {
    suspend fun enqueueAction(action: OutboxAction): Result<Unit, AppError>
    suspend fun processPendingActions(): Result<Int, AppError>
    suspend fun retryFailedAction(actionId: String): Result<Unit, AppError>
    suspend fun cancelAction(actionId: String): Result<Unit, AppError>
    
    fun observeActionStatus(actionId: String): Flow<Result<ActionStatus, AppError>>
    fun observePendingActions(): Flow<Result<List<OutboxAction>, AppError>>
    suspend fun cleanupCompletedActions(olderThan: Long): Result<Int, AppError>
}

data class OutboxAction(
    val id: String,
    val type: ActionType,
    val payload: Map<String, Any>,
    val priority: Priority,
    val maxRetries: Int,
    val idempotencyKey: String?,
    val targetEntityId: String?,
    val createdAt: Long,
    val scheduledFor: Long
)

enum class ActionType(val apiEndpoint: String) {
    USER_INIT("/users.me.init"),
    USER_UPDATE("/users.me"),
    DEVICE_CLAIM("/devices.claim"),
    HUG_SEND("/hugs.send"),
    PATTERN_CREATE("/patterns"),
    PRACTICE_START("/practices/{id}/start"),
    // ... другие типы действий
}

enum class Priority(val value: Int) {
    LOW(10),
    NORMAL(50),
    HIGH(100),
    CRITICAL(200)
}
```

## Контракты для кэширования

### 1. CacheManager

```kotlin
// :shared/domain/cache/CacheManager.kt
interface CacheManager {
    suspend fun <T> get(key: String, type: Class<T>): Result<T?, AppError>
    suspend fun <T> put(key: String, value: T, ttl: Long? = null): Result<Unit, AppError>
    suspend fun delete(key: String): Result<Unit, AppError>
    suspend fun clear(): Result<Unit, AppError>
    suspend fun invalidatePattern(pattern: String): Result<Int, AppError>
    
    fun observeCacheMetrics(): Flow<Result<CacheMetrics, AppError>>
    suspend fun getCacheSize(): Result<Long, AppError>
}

data class CacheMetrics(
    val hitRate: Double,
    val missRate: Double,
    val evictionCount: Long,
    val totalSize: Long,
    val entryCount: Int
)
```

### 2. CachePolicy

```kotlin
// :shared/domain/cache/CachePolicy.kt
interface CachePolicy {
    fun getTTL(key: String): Long?
    fun shouldCache(key: String): Boolean
    fun getMaxSize(): Long
    fun getEvictionStrategy(): EvictionStrategy
}

enum class EvictionStrategy {
    LRU, // Least Recently Used
    LFU, // Least Frequently Used
    TTL, // Time To Live
    SIZE_BASED
}
```

## Контракты для синхронизации

### 1. SyncManager

```kotlin
// :shared/domain/sync/SyncManager.kt
interface SyncManager {
    suspend fun syncAll(): Result<SyncResult, AppError>
    suspend fun syncEntity(entityType: EntityType): Result<SyncResult, AppError>
    suspend fun forceSync(): Result<SyncResult, AppError>
    
    fun observeSyncStatus(): Flow<Result<SyncStatus, AppError>>
    suspend fun getLastSyncTime(entityType: EntityType): Result<Long?, AppError>
}

data class SyncResult(
    val entityType: EntityType,
    val itemsSynced: Int,
    val itemsCreated: Int,
    val itemsUpdated: Int,
    val itemsDeleted: Int,
    val errors: List<SyncError>,
    val duration: Long
)

enum class EntityType {
    USERS, DEVICES, HUGS, PATTERNS, PRACTICES, RULES
}
```

### 2. ConflictResolver

```kotlin
// :shared/domain/sync/ConflictResolver.kt
interface ConflictResolver<T> {
    suspend fun resolveConflict(
        local: T,
        remote: T,
        conflictType: ConflictType
    ): Result<T, AppError>
    
    suspend fun detectConflict(local: T, remote: T): Result<ConflictType?, AppError>
}

enum class ConflictType {
    VERSION_MISMATCH,
    CONCURRENT_MODIFICATION,
    DELETION_CONFLICT,
    VALIDATION_CONFLICT
}
```

## Контракты для безопасности

### 1. EncryptionManager

```kotlin
// :shared/domain/security/EncryptionManager.kt
interface EncryptionManager {
    suspend fun encrypt(data: String): Result<String, AppError>
    suspend fun decrypt(encryptedData: String): Result<String, AppError>
    suspend fun encryptSensitive(data: Map<String, Any>): Result<Map<String, String>, AppError>
    suspend fun decryptSensitive(encryptedData: Map<String, String>): Result<Map<String, Any>, AppError>
    
    suspend fun rotateKeys(): Result<Unit, AppError>
    suspend fun getKeyStatus(): Result<KeyStatus, AppError>
}

data class KeyStatus(
    val currentKeyVersion: Int,
    val keyRotationDate: Long,
    val nextRotationDate: Long?,
    val isHealthy: Boolean
)
```

### 2. AuditLogger

```kotlin
// :shared/domain/security/AuditLogger.kt
interface AuditLogger {
    suspend fun logAction(action: AuditAction): Result<Unit, AppError>
    suspend fun logDataAccess(entityType: String, entityId: String, operation: String): Result<Unit, AppError>
    suspend fun logSecurityEvent(event: SecurityEvent): Result<Unit, AppError>
    
    suspend fun getAuditTrail(
        entityType: String? = null,
        entityId: String? = null,
        from: Long? = null,
        to: Long? = null,
        limit: Int = 100
    ): Result<List<AuditEntry>, AppError>
}

data class AuditAction(
    val userId: String,
    val action: String,
    val entityType: String?,
    val entityId: String?,
    val details: Map<String, Any>,
    val timestamp: Long,
    val ipAddress: String?,
    val userAgent: String?
)
```

## Контракты для мониторинга

### 1. HealthChecker

```kotlin
// :shared/domain/monitoring/HealthChecker.kt
interface HealthChecker {
    suspend fun checkHealth(): Result<HealthStatus, AppError>
    suspend fun checkDatabaseHealth(): Result<DatabaseHealth, AppError>
    suspend fun checkNetworkHealth(): Result<NetworkHealth, AppError>
    suspend fun checkCacheHealth(): Result<CacheHealth, AppError>
    
    fun observeHealthStatus(): Flow<Result<HealthStatus, AppError>>
}

data class HealthStatus(
    val overall: HealthLevel,
    val components: Map<String, ComponentHealth>,
    val lastChecked: Long,
    val uptime: Long
)

enum class HealthLevel {
    HEALTHY, DEGRADED, UNHEALTHY, UNKNOWN
}
```

### 2. MetricsCollector

```kotlin
// :shared/domain/monitoring/MetricsCollector.kt
interface MetricsCollector {
    suspend fun recordMetric(metric: Metric): Result<Unit, AppError>
    suspend fun recordCounter(name: String, value: Long, tags: Map<String, String> = emptyMap()): Result<Unit, AppError>
    suspend fun recordTimer(name: String, duration: Long, tags: Map<String, String> = emptyMap()): Result<Unit, AppError>
    suspend fun recordGauge(name: String, value: Double, tags: Map<String, String> = emptyMap()): Result<Unit, AppError>
    
    suspend fun getMetrics(name: String, from: Long, to: Long): Result<List<MetricPoint>, AppError>
}

data class Metric(
    val name: String,
    val value: Double,
    val timestamp: Long,
    val tags: Map<String, String>,
    val type: MetricType
)

enum class MetricType {
    COUNTER, GAUGE, TIMER, HISTOGRAM
}
```

## Контракты для конфигурации

### 1. ConfigRepository

```kotlin
// :shared/domain/config/ConfigRepository.kt
interface ConfigRepository {
    suspend fun getString(key: String, defaultValue: String = ""): Result<String, AppError>
    suspend fun getBoolean(key: String, defaultValue: Boolean = false): Result<Boolean, AppError>
    suspend fun getInt(key: String, defaultValue: Int = 0): Result<Int, AppError>
    suspend fun getLong(key: String, defaultValue: Long = 0L): Result<Long, AppError>
    suspend fun getDouble(key: String, defaultValue: Double = 0.0): Result<Double, AppError>
    suspend fun getJson(key: String): Result<JsonElement, AppError>
    
    fun observeConfig(): Flow<Result<AppConfig, AppError>>
    suspend fun refreshConfig(): Result<Unit, AppError>
    suspend fun setConfig(key: String, value: Any): Result<Unit, AppError>
}

data class AppConfig(
    val version: String,
    val lastUpdated: Long,
    val values: Map<String, Any>,
    val featureFlags: Map<String, Boolean>
)
```

## Контракты для работы с файлами

### 1. FileRepository

```kotlin
// :shared/domain/file/FileRepository.kt
interface FileRepository {
    suspend fun uploadFile(file: FileData, metadata: FileMetadata): Result<FileInfo, AppError>
    suspend fun downloadFile(fileId: String): Result<FileData, AppError>
    suspend fun deleteFile(fileId: String): Result<Unit, AppError>
    suspend fun getFileInfo(fileId: String): Result<FileInfo, AppError>
    
    suspend fun getFileUrl(fileId: String, expiresIn: Long = 3600): Result<String, AppError>
    suspend fun listFiles(prefix: String? = null, limit: Int = 100): Result<List<FileInfo>, AppError>
}

data class FileData(
    val content: ByteArray,
    val mimeType: String,
    val size: Long
)

data class FileMetadata(
    val name: String,
    val description: String?,
    val tags: List<String>,
    val isPublic: Boolean
)

data class FileInfo(
    val id: String,
    val name: String,
    val mimeType: String,
    val size: Long,
    val uploadedAt: Long,
    val metadata: FileMetadata
)
```

## Контракты для работы с уведомлениями

### 1. NotificationRepository

```kotlin
// :shared/domain/notification/NotificationRepository.kt
interface NotificationRepository {
    suspend fun registerToken(token: String, platform: Platform): Result<Unit, AppError>
    suspend fun unregisterToken(token: String): Result<Unit, AppError>
    suspend fun sendNotification(notification: Notification): Result<Unit, AppError>
    suspend fun sendToUser(userId: String, notification: Notification): Result<Unit, AppError>
    
    fun observeNotifications(): Flow<Result<List<Notification>, AppError>>
    suspend fun markAsRead(notificationId: String): Result<Unit, AppError>
    suspend fun markAllAsRead(): Result<Unit, AppError>
}

data class Notification(
    val id: String,
    val title: String,
    val body: String,
    val data: Map<String, String>,
    val type: NotificationType,
    val priority: NotificationPriority,
    val createdAt: Long
)

enum class Platform {
    ANDROID, IOS, WEB
}

enum class NotificationType {
    HUG_RECEIVED, PRACTICE_REMINDER, DEVICE_UPDATE, SYSTEM_ANNOUNCEMENT
}
```

## Стратегии обработки ошибок

### 1. Централизованная обработка

```kotlin
// :core:network/SafeApiCall.kt
suspend fun <T> safeApiCall(
    apiCall: suspend () -> T,
    errorMapper: ErrorMapper = DefaultErrorMapper()
): Result<T, AppError> {
    return try {
        Result.success(apiCall())
    } catch (e: CancellationException) {
        throw e // Пробрасываем отмену корутин
    } catch (e: HttpException) {
        Result.failure(errorMapper.mapHttpException(e))
    } catch (e: IOException) {
        Result.failure(AppError.Network)
    } catch (e: Exception) {
        Result.failure(AppError.Unknown)
    }
}
```

### 2. Retry стратегии

```kotlin
// :shared/domain/retry/RetryStrategy.kt
interface RetryStrategy {
    suspend fun <T> executeWithRetry(
        operation: suspend () -> Result<T, AppError>,
        maxRetries: Int = 3,
        baseDelay: Long = 1000L
    ): Result<T, AppError>
}

class ExponentialBackoffRetryStrategy : RetryStrategy {
    override suspend fun <T> executeWithRetry(
        operation: suspend () -> Result<T, AppError>,
        maxRetries: Int,
        baseDelay: Long
    ): Result<T, AppError> {
        var currentDelay = baseDelay
        repeat(maxRetries) { attempt ->
            when (val result = operation()) {
                is Result.Success -> return result
                is Result.Failure -> {
                    if (shouldRetry(result.error)) {
                        delay(currentDelay)
                        currentDelay = (currentDelay * 2).coerceAtMost(30000L) // Max 30 seconds
                    } else {
                        return result
                    }
                }
            }
        }
        return operation() // Последняя попытка
    }
    
    private fun shouldRetry(error: AppError): Boolean {
        return when (error) {
            is AppError.Network, is AppError.Timeout, is AppError.Server -> true
            else -> false
        }
    }
}
```

## Мониторинг и метрики

### 1. Метрики производительности

```kotlin
// :data:common/PerformanceMetrics.kt
class PerformanceMetrics(
    private val metricsCollector: MetricsCollector
) {
    suspend fun <T> measureOperation(
        operationName: String,
        operation: suspend () -> T
    ): T {
        val startTime = System.currentTimeMillis()
        return try {
            val result = operation()
            val duration = System.currentTimeMillis() - startTime
            metricsCollector.recordTimer(operationName, duration)
            result
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            metricsCollector.recordTimer("$operationName.error", duration)
            throw e
        }
    }
}
```

### 2. Health checks

```kotlin
// :data:common/HealthCheckerImpl.kt
class HealthCheckerImpl(
    private val database: AppDatabase,
    private val cacheManager: CacheManager,
    private val networkChecker: NetworkChecker
) : HealthChecker {
    
    override suspend fun checkHealth(): Result<HealthStatus, AppError> {
        val components = mutableMapOf<String, ComponentHealth>()
        
        // Проверка базы данных
        val dbHealth = checkDatabaseHealth()
        components["database"] = dbHealth.getOrElse { 
            ComponentHealth(HealthLevel.UNHEALTHY, it.message ?: "Unknown error")
        }
        
        // Проверка кэша
        val cacheHealth = checkCacheHealth()
        components["cache"] = cacheHealth.getOrElse { 
            ComponentHealth(HealthLevel.UNHEALTHY, it.message ?: "Unknown error")
        }
        
        // Проверка сети
        val networkHealth = checkNetworkHealth()
        components["network"] = networkHealth.getOrElse { 
            ComponentHealth(HealthLevel.UNHEALTHY, it.message ?: "Unknown error")
        }
        
        val overallHealth = determineOverallHealth(components.values)
        
        return Result.success(
            HealthStatus(
                overall = overallHealth,
                components = components,
                lastChecked = System.currentTimeMillis(),
                uptime = getUptime()
            )
        )
    }
}
```

## Заключение

Данные контракты обеспечивают:

1. **Единообразие**: Все репозитории следуют единым паттернам и принципам
2. **Надежность**: Централизованная обработка ошибок и retry-логика
3. **Производительность**: Оптимизированные стратегии кэширования и синхронизации
4. **Безопасность**: Шифрование данных и аудит всех операций
5. **Масштабируемость**: Поддержка пагинации, очередей и мониторинга
6. **Тестируемость**: Четкие интерфейсы для легкого мокирования в тестах

Эти контракты служат основой для создания enterprise-уровня data слоя, который может масштабироваться и адаптироваться к изменяющимся требованиям бизнеса.
