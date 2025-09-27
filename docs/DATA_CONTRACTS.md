# Контракты Enterprise Data слоя

Данный документ определяет строгие контракты для всех репозиториев в Data слое приложения Amulet, обеспечивая полную изоляцию Domain слоя от деталей реализации источников данных.

## Золотое правило контрактов репозитория

**Репозиторий оперирует только доменными моделями.**

- **На входе (в параметрах):** Принимает либо примитивные типы (`id: String`, `limit: Int`), либо **доменные модели** (`user: User`, `patternUpdate: PatternUpdate`). Он **никогда** не должен принимать DTO или Entity.
- **На выходе (возвращаемое значение):** Всегда возвращает либо `Unit` (для операций, где результат не важен), примитивные типы, либо **доменные модели** (`Result<User, AppError>`, `Flow<List<Hug>>`). Он **никогда** не должен возвращать DTO или Entity.

Это обеспечивает полную изоляцию `Domain` слоя от деталей реализации `Data` слоя.

---

## Категории методов репозитория и их сигнатуры

### 1. Получение одиночной сущности (Read One)

Запрос одной сущности по ее идентификатору. Обычно это `suspend` функция.

**Назначение:** Загрузить или получить из кэша один объект.

**Сигнатура:**
```kotlin
// В :shared/domain/repository/PatternsRepository.kt
interface PatternsRepository {
    suspend fun getPatternById(id: String): Result<Pattern, AppError>
}
```

**Параметры:**
- `id: String`: Простой идентификатор.

**Возвращаемое значение:**
- `Result<Pattern, AppError>`: Оборачиваем в `Result`, так как запрос может провалиться (нет сети, паттерн не найден → `AppError.NotFound`).

**Логика реализации в `:data`:**
1. Попытаться получить из локальной БД (Room).
2. Если в БД нет или данные устарели (политика stale-while-revalidate), сделать сетевой запрос `safeApiCall { api.getPattern(id) }`.
3. При успехе: смапить DTO в Entity, сохранить в БД, смапить DTO в доменную модель и вернуть.
4. При ошибке: вернуть `Result.failure(appError)`.

### 2. Наблюдение за одиночной сущностью (Observe One)

Реактивное получение одной сущности. Используется для экранов, которые должны мгновенно обновляться при изменении данных.

**Назначение:** Подписаться на изменения конкретного объекта в БД.

**Сигнатура:**
```kotlin
// В :shared/domain/repository/UserRepository.kt
interface UserRepository {
    fun observeMyProfile(): Flow<User?> // Может быть null, если пользователь не залогинен
}
```

**Параметры:** Обычно нет, так как "текущий" пользователь один.

**Возвращаемое значение:**
- `Flow<User?>`: Возвращаем `Flow` доменной модели. Она может быть `nullable`, если сущности может не быть. Ошибки БД обрабатываются внутри `asResult()`.

**Логика реализации в `:data`:**
1. Вернуть `Flow` из DAO: `userDao.observeCurrentUser().map { it?.toDomain() }`.
2. Опционально: в `ViewModel` или `UseCase` этот поток можно обернуть в `asResult()` для обработки ошибок чтения из БД.

### 3. Получение списка/коллекции (Read Many / Paginated)

Получение списка сущностей, часто с пагинацией.

**Назначение:** Предоставить данные для "бесконечных" лент.

**Сигнатура:**
```kotlin
// В :shared/domain/repository/HugsRepository.kt
interface HugsRepository {
    fun getHugsPagingSource(direction: HugDirection): PagingSource<String, Hug>
}

// Вспомогательный enum в :shared
enum class HugDirection { SENT, RECEIVED }
```

**Параметры:**
- `direction: HugDirection`: Параметр для фильтрации, тоже доменная модель (enum).

**Возвращаемое значение:**
- `PagingSource<String, Hug>`: Возвращаем `PagingSource` для библиотеки Paging 3. Он оперирует ключом пагинации (`String` - наш `cursor`) и **доменной моделью** (`Hug`).

**Логика реализации в `:data`:**
1. Репозиторий будет реализовывать `RemoteMediator`, который управляет загрузкой данных из сети, сохранением в БД и обновлением `remote_keys`.
2. Метод `getHugsPagingSource` просто вернет `PagingSource` из соответствующего DAO: `hugsDao.pagingSourceReceived(myUserId)`. Paging 3 сделает всю магию за вас.

### 4. Операции изменения данных (Create, Update, Delete)

Все операции, которые меняют состояние на бэкенде. Это всегда `suspend` функции.

**Назначение:** Отправить команду на сервер и, опционально, обновить локальное состояние.

**Сигнатура (Create):**
```kotlin
// В :shared/domain/repository/PatternsRepository.kt
interface PatternsRepository {
    suspend fun createPattern(newPattern: NewPatternData): Result<Pattern, AppError>
}

// Вспомогательная доменная модель для создания
data class NewPatternData(
    val kind: String,
    val spec: PatternSpec,
    // ... другие поля
)
```

**Сигнатура (Update):**
```kotlin
// В :shared/domain/repository/PatternsRepository.kt
interface PatternsRepository {
    suspend fun updatePattern(patternUpdate: PatternUpdate): Result<Pattern, AppError>
}

// Вспомогательная доменная модель для обновления
data class PatternUpdate(
    val id: String,
    val editingVersion: Int, // Для optimistic locking
    val newTitle: String?,
    val newSpec: PatternSpec?
)
```

**Сигнатура (Delete):**
```kotlin
// В :shared/domain/repository/PatternsRepository.kt
interface PatternsRepository {
    suspend fun deletePattern(id: String): Result<Unit, AppError>
}
```

**Параметры:**
- Принимают либо ID для удаления, либо специальные **доменные модели** для создания/обновления (`NewPatternData`, `PatternUpdate`). Это позволяет `Domain` слою не знать о структуре DTO.

**Возвращаемое значение:**
- `Result<Pattern, AppError>`: При создании/обновлении сервер обычно возвращает созданную/обновленную сущность.
- `Result<Unit, AppError>`: При удалении нам не нужны данные, только подтверждение успеха.

**Логика реализации в `:data`:**
1. Смапить доменную модель (`PatternUpdate`) в DTO (`PatternUpdateRequestDto`).
2. Вызвать `safeApiCall { api.updatePattern(id, dto) }`.
3. При успехе: смапить DTO ответа в Entity, обновить/вставить в БД, смапить в доменную модель и вернуть.
4. Для **offline-first** (`outbox`): этот метод не будет вызывать API напрямую. Он создаст запись в таблице `outbox_actions` и оптимистично обновит локальную БД. `WorkManager` позже выполнит реальный сетевой запрос.

---

## Сводная таблица контрактов

| Тип операции                  | Пример метода в интерфейсе репозитория                        | Параметры (вход)                      | Возвращаемое значение (выход)                  |
| ----------------------------- | ------------------------------------------------------------- | ------------------------------------- | ---------------------------------------------- |
| **Чтение (один объект)**      | `suspend fun getPattern(id: String)`                          | `id: String` (Примитив)               | `Result<Pattern, AppError>` (Доменная модель)  |
| **Наблюдение (один объект)**  | `fun observeUserProfile()`                                    | Нет                                   | `Flow<User?>` (Доменная модель)                |
| **Чтение (список/пагинация)** | `fun getHugsPagingSource(direction: HugDirection)`            | `direction: HugDirection` (Домен. enum) | `PagingSource<String, Hug>` (Доменная модель)  |
| **Создание**                  | `suspend fun createPattern(data: NewPatternData)`             | `data: NewPatternData` (Домен. модель)  | `Result<Pattern, AppError>` (Доменная модель)  |
| **Обновление**                | `suspend fun updatePattern(update: PatternUpdate)`            | `update: PatternUpdate` (Домен. модель) | `Result<Pattern, AppError>` (Доменная модель)  |
| **Удаление**                  | `suspend fun deleteDevice(id: String)`                        | `id: String` (Примитив)               | `Result<Unit, AppError>` (Нет данных)          |

---

## Специфичные контракты для Amulet App

### UserRepository

```kotlin
interface UserRepository {
    // Профиль пользователя
    suspend fun getMyProfile(): Result<User, AppError>
    fun observeMyProfile(): Flow<User?>
    suspend fun updateProfile(update: UserUpdate): Result<User, AppError>
    suspend fun initializeProfile(init: UserInit): Result<User, AppError>
    
    // Согласия и настройки
    suspend fun updateConsents(consents: UserConsents): Result<Unit, AppError>
    fun observeConsents(): Flow<UserConsents?>
}
```

### DevicesRepository

```kotlin
interface DevicesRepository {
    // Управление устройствами
    suspend fun claimDevice(claimRequest: DeviceClaimRequest): Result<Device, AppError>
    suspend fun getMyDevices(): Result<List<Device>, AppError>
    fun observeMyDevices(): Flow<List<Device>>
    suspend fun getDeviceById(id: String): Result<Device, AppError>
    suspend fun updateDevice(id: String, update: DeviceUpdate): Result<Device, AppError>
    suspend fun unclaimDevice(id: String): Result<Unit, AppError>
    
    // OTA обновления
    suspend fun checkFirmwareUpdate(hardwareVersion: Int, currentFirmware: String): Result<FirmwareInfo?, AppError>
    suspend fun reportFirmwareInstallation(deviceId: String, report: FirmwareReport): Result<Unit, AppError>
}
```

### HugsRepository

```kotlin
interface HugsRepository {
    // Отправка объятий
    suspend fun sendHug(request: SendHugRequest): Result<Hug, AppError>
    
    // История объятий
    fun getHugsPagingSource(direction: HugDirection): PagingSource<String, Hug>
    suspend fun getHugById(id: String): Result<Hug, AppError>
    
    // Обработка входящих объятий (FCM)
    suspend fun onHugReceived(hugId: String, payload: Map<String, Any>?): Result<Unit, AppError>
}
```

### PatternsRepository

```kotlin
interface PatternsRepository {
    // CRUD операции
    suspend fun getPatternById(id: String): Result<Pattern, AppError>
    suspend fun createPattern(newPattern: NewPatternData): Result<Pattern, AppError>
    suspend fun updatePattern(update: PatternUpdate): Result<Pattern, AppError>
    suspend fun deletePattern(id: String): Result<Unit, AppError>
    
    // Каталоги
    fun getPublicPatternsPagingSource(filters: PatternFilters): PagingSource<String, Pattern>
    suspend fun getMyPatterns(): Result<List<Pattern>, AppError>
    
    // Предпросмотр и шаринг
    suspend fun previewPattern(deviceId: String, spec: PatternSpec): Result<String, AppError>
    suspend fun sharePattern(patternId: String, shareRequest: PatternShareRequest): Result<Unit, AppError>
}
```

### PracticesRepository

```kotlin
interface PracticesRepository {
    // Каталог практик
    fun getPracticesPagingSource(filters: PracticeFilters): PagingSource<String, Practice>
    suspend fun getPracticeById(id: String): Result<Practice, AppError>
    
    // Сессии
    suspend fun startPractice(practiceId: String, request: PracticeStartRequest): Result<PracticeSession, AppError>
    suspend fun stopSession(sessionId: String, request: PracticeStopRequest): Result<PracticeSummary, AppError>
    fun observeActiveSessions(): Flow<List<PracticeSession>>
}
```

### PairsRepository

```kotlin
interface PairsRepository {
    // Управление парами
    suspend fun invitePartner(request: PairInviteRequest): Result<PairInvite, AppError>
    suspend fun acceptInvitation(request: PairAcceptRequest): Result<Pair, AppError>
    suspend fun getMyPairs(): Result<List<Pair>, AppError>
    fun observeMyPairs(): Flow<List<Pair>>
    
    // Блокировка
    suspend fun blockPair(pairId: String): Result<Pair, AppError>
    suspend fun unblockPair(pairId: String): Result<Pair, AppError>
}
```

### RulesRepository

```kotlin
interface RulesRepository {
    // CRUD правил
    suspend fun getRules(): Result<List<Rule>, AppError>
    fun observeRules(): Flow<List<Rule>>
    suspend fun createRule(request: RuleCreateRequest): Result<Rule, AppError>
    suspend fun updateRule(ruleId: String, update: RuleUpdateRequest): Result<Rule, AppError>
    suspend fun deleteRule(ruleId: String): Result<Unit, AppError>
    
    // Выполнение локальных правил
    suspend fun evaluateLocalTriggers(event: LocalEvent): Result<Unit, AppError>
}
```

### PrivacyRepository

```kotlin
interface PrivacyRepository {
    // GDPR операции
    suspend fun requestDataExport(): Result<PrivacyJob, AppError>
    suspend fun requestDataDeletion(): Result<PrivacyJob, AppError>
    suspend fun getExportStatus(jobId: String): Result<PrivacyJob, AppError>
    suspend fun getPrivacyRights(): Result<PrivacyRights, AppError>
    suspend fun getAuditLog(operation: String?, limit: Int?): Result<List<PrivacyAuditEntry>, AppError>
}
```

### TelemetryRepository

```kotlin
interface TelemetryRepository {
    // Отправка событий
    suspend fun reportEvents(events: List<TelemetryEvent>): Result<Unit, AppError>
    
    // Локальная очередь
    suspend fun enqueueEvent(event: TelemetryEvent): Result<Unit, AppError>
    fun observePendingEvents(): Flow<List<TelemetryEvent>>
}
```

---

## Обработка ошибок в контрактах

Все методы репозиториев, которые могут завершиться ошибкой, должны возвращать `Result<T, AppError>`. Централизованная обработка ошибок обеспечивается через:

### SafeApiCall для сетевых операций

```kotlin
// В :core:network/util/SafeApiCall.kt
suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T, AppError> {
    return try {
        Result.success(apiCall())
    } catch (e: HttpException) {
        val appError = when (e.code()) {
            401 -> AppError.Unauthorized
            403 -> AppError.Forbidden
            404 -> AppError.NotFound
            409 -> AppError.Conflict
            429 -> AppError.RateLimited
            412 -> AppError.PreconditionFailed(parseReason(e))
            in 400..499 -> AppError.Validation(parseValidationErrors(e))
            else -> AppError.Server(e.code(), e.message())
        }
        Result.failure(appError)
    } catch (e: IOException) {
        Result.failure(AppError.Network)
    } catch (e: Exception) {
        Result.failure(AppError.Unknown)
    }
}
```

### AsResult для Flow операций

```kotlin
// В :shared/domain/util/FlowExtensions.kt
fun <T> Flow<T>.asResult(): Flow<Result<T, AppError>> {
    return this
        .map<T, Result<T, AppError>> { Result.success(it) }
        .catch { e ->
            val error = when(e) {
                is IOException -> AppError.Network
                is SQLiteException -> AppError.DatabaseError
                else -> AppError.Unknown
            }
            emit(Result.failure(error))
        }
}
```

---

## Offline-first стратегия

### Outbox Actions для исходящих операций

Все операции изменения данных (Create, Update, Delete) должны поддерживать offline-first через очередь `outbox_actions`:

```kotlin
// Пример для отправки объятия
suspend fun sendHug(request: SendHugRequest): Result<Hug, AppError> {
    return try {
        // 1. Оптимистично обновляем локальное состояние
        val localHug = createLocalHug(request)
        hugsDao.insert(localHug)
        
        // 2. Добавляем в очередь исходящих действий
        val outboxAction = OutboxAction(
            id = generateId(),
            type = OutboxActionType.HUG_SEND,
            payloadJson = serialize(request),
            status = OutboxActionStatus.PENDING,
            priority = OutboxActionPriority.HIGH
        )
        outboxDao.insert(outboxAction)
        
        Result.success(localHug)
    } catch (e: Exception) {
        Result.failure(AppError.DatabaseError)
    }
}
```

### Stale-while-revalidate для чтения

```kotlin
suspend fun getPatternById(id: String): Result<Pattern, AppError> {
    return try {
        // 1. Сначала пытаемся получить из локальной БД
        val localPattern = patternsDao.getById(id)?.toDomain()
        
        if (localPattern != null && !isStale(localPattern.updatedAt)) {
            return Result.success(localPattern)
        }
        
        // 2. Если данных нет или они устарели, загружаем с сервера
        val networkResult = safeApiCall { apiService.getPattern(id) }
        
        when (networkResult) {
            is Result.Success -> {
                val pattern = networkResult.value.toDomain()
                patternsDao.upsert(pattern.toEntity())
                Result.success(pattern)
            }
            is Result.Failure -> {
                // Возвращаем локальные данные, если есть, иначе ошибку
                localPattern?.let { Result.success(it) } ?: Result.failure(networkResult.error)
            }
        }
    } catch (e: Exception) {
        Result.failure(AppError.DatabaseError)
    }
}
```

---

## Требования к реализации

### 1. Маппинг между слоями

Каждый `:data:*` модуль должен содержать мапперы:

```kotlin
// DTO → Domain
fun PatternDto.toDomain(): Pattern = Pattern(
    id = this.id,
    title = this.title,
    spec = this.spec.toDomain(),
    // ...
)

// Domain → Entity
fun Pattern.toEntity(): PatternEntity = PatternEntity(
    id = this.id,
    title = this.title,
    specJson = this.spec.toJson(),
    // ...
)

// Entity → Domain
fun PatternEntity.toDomain(): Pattern = Pattern(
    id = this.id,
    title = this.title,
    spec = this.specJson.toPatternSpec(),
    // ...
)
```

### 2. Идемпотентность

Все операции должны быть идемпотентными:

```kotlin
// Использование idempotency key
suspend fun sendHug(request: SendHugRequest): Result<Hug, AppError> {
    val idempotencyKey = generateIdempotencyKey(request)
    
    return safeApiCall {
        apiService.sendHug(request, idempotencyKey)
    }.map { it.toDomain() }
}
```

### 3. Консистентность данных

```kotlin
// Транзакционные операции
@Transaction
suspend fun updatePatternWithTags(pattern: Pattern, tags: List<String>) {
    patternDao.update(pattern.toEntity())
    patternTagsDao.deleteByPatternId(pattern.id)
    patternTagsDao.insertAll(tags.map { PatternTagEntity(pattern.id, it) })
}
```

### 4. Кэширование и TTL

```kotlin
// Политики кэширования
private fun isStale(updatedAt: Long): Boolean {
    val ttl = 5 * 60 * 1000L // 5 минут
    return System.currentTimeMillis() - updatedAt > ttl
}
```

---

## Тестирование контрактов

### Unit тесты для репозиториев

```kotlin
class PatternsRepositoryTest {
    @Test
    fun `getPatternById should return domain model`() = runTest {
        // Given
        val patternId = "test-id"
        val expectedPattern = Pattern(id = patternId, title = "Test")
        coEvery { apiService.getPattern(patternId) } returns PatternDto(id = patternId, title = "Test")
        
        // When
        val result = repository.getPatternById(patternId)
        
        // Then
        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat((result as Result.Success).value).isEqualTo(expectedPattern)
    }
}
```

### Интеграционные тесты

```kotlin
class PatternsRepositoryIntegrationTest {
    @Test
    fun `should handle offline scenario correctly`() = runTest {
        // Given - нет сети
        networkMonitor.isOnline = false
        
        // When
        val result = repository.getPatternById("test-id")
        
        // Then - должен вернуть локальные данные или ошибку
        assertThat(result).isInstanceOf(Result::class.java)
    }
}
```

---

## Заключение

Данные контракты обеспечивают:

1. **Полную изоляцию** Domain слоя от деталей реализации Data слоя
2. **Предсказуемость** API репозиториев через строгие типы
3. **Тестируемость** через четкие интерфейсы
4. **Offline-first** подход через outbox actions и stale-while-revalidate
5. **Консистентность** обработки ошибок через централизованные утилиты

Следование этим контрактам гарантирует, что приложение Amulet будет масштабируемым, надежным и легко тестируемым.
