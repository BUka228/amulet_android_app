## Сетевое взаимодействие и контракт с API

Цель: определить стандарты, инструменты и архитектуру для взаимодействия с публичным API Amulet. Документ описывает настройку сетевого слоя, структуру DTO, правила маппинга и стратегию обработки данных, полученных от бэкенда.

Связанные документы: см. `docs/10_ARCHITECTURE/01_ARCHITECTURE_OVERVIEW.md`, `docs/10_ARCHITECTURE/02_MODULARIZATION.md`, `docs/50_CROSS_CUTTING_CONCERNS/01_ERROR_HANDLING.md`, `docs/50_CROSS_CUTTING_CONCERNS/02_LOGGING_AND_TELEMETRY.md`, `docs/20_DATA_LAYER/01_DATABASE.md`, `docs/20_DATA_LAYER/04_OFFLINE_SYNC.md`.

---

### 1. Технологический стек и конфигурация (`:core:network`)

- **HTTP‑клиент:** OkHttp
- **REST‑адаптер:** Retrofit
- **Сериализация:** kotlinx.serialization (`kotlinx-serialization-json`). Это обязательный стандарт; Gson/Moshi не используются, чтобы обеспечить KMP‑совместимость и единый стек сериализации.

#### 1.1. Конфигурация OkHttpClient

- **Таймауты по умолчанию:**
  - `connectTimeout = 15_000 ms`
  - `readTimeout = 30_000 ms`
  - `writeTimeout = 30_000 ms`
- **Логирование:** `HttpLoggingInterceptor`
  - `Level.BODY` в debug‑сборках
  - `Level.NONE` в release‑сборках (обязательно для безопасности и приватности; см. `02_LOGGING_AND_TELEMETRY.md` о запрете PII)
- **Интерсепторы и порядок:**
  1) `AuthInterceptor` — добавляет `Authorization: Bearer <id_token>` (токен обновляется через Firebase SDK; см. `01_ARCHITECTURE_OVERVIEW.md`, ADR об аутентификации)
  2) `AppCheckInterceptor` — добавляет `X-Firebase-AppCheck: <token>`
  3) `HttpLoggingInterceptor` — только в debug
  4) `ErrorHandlingInterceptor` (опционально) — централизованная обработка HTTP‑ошибок, если не используется `safeApiCall` на каждом вызове. В нашем стандарте предпочтительнее `safeApiCall`.

#### 1.2. Конфигурация Retrofit

- **Base URL:** управляется через `BuildConfig` и productFlavors: `debug`, `staging`, `release`.
  - Пример: `BuildConfig.API_BASE_URL`
- **Converter Factory:** `Json.asConverterFactory("application/json".toMediaType())`
- **CallAdapter:** стандартные suspend‑функции Retrofit. Возврат исключений перехватывается в `safeApiCall`.

#### 1.3. Dependency Injection (Hilt)

Модуль `:core:network` предоставляет синглтоны `OkHttpClient`, `Retrofit` и реализации `*ApiService`. Пример (эскиз):

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun provideOkHttpClient(
        auth: AuthInterceptor,
        appCheck: AppCheckInterceptor,
        logger: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(auth)
        .addInterceptor(appCheck)
        .apply { if (BuildConfig.DEBUG) addInterceptor(logger.setLevel(Level.BODY)) }
        .build()

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .build()
}
```

См. правила модульности: `:data:*` зависят от `:core:network` (см. `02_MODULARIZATION.md`).

---

### 2. Структура моделей и DTO (Data Transfer Objects)

- **Расположение:** все DTO по схемам OpenAPI v1 располагаются в `:shared` (KMP). Это часть публичного контракта.
- **Нейминг:**
  - Суффикс `Dto` для всех типов, отражающих JSON: `UserDto`, `HugDto`, `PatternDto`.
  - Аннотации `@Serializable` и `@SerialName` для полей, чьи имена отличаются от Kotlin‑конвенций (`snake_case` → `camelCase`).
- **Обработка Timestamp:** в OpenAPI возможны варианты: ISO‑строка или `{ _seconds, _nanoseconds }`. В `:shared` используется кастомный `KSerializer` (например, `TimestampAsEpochMillisSerializer`), который десериализует обе формы в `Long` (epochMillis) и сериализует обратно в ISO‑строку при необходимости.
- **Полиморфные типы:** для иерархий вроде `PatternElement*` используется `JsonContentPolymorphicSerializer` c дискриминатором `type`. Неизвестные типы должны падать безопасно с `SerializationException` и маппиться в `AppError.Unknown` на границе сети.

Пример DTO (эскиз):

```kotlin
@Serializable
data class HugDto(
    val id: String,
    @SerialName("from_user_id") val fromUserId: String,
    @SerialName("created_at") @Serializable(with = TimestampAsEpochMillisSerializer::class)
    val createdAt: Long
)
```

---

### 3. Правила маппинга DTO ↔ Domain Model

- **Золотое правило:** маппинг происходит в `Data` слое, внутри `*RepositoryImpl`. DTO не «протекают» в Domain/Presentation (см. `01_ARCHITECTURE_OVERVIEW.md`).
- **Расположение мапперов:** функции‑расширения в `:data:*` рядом с репозиториями: `UserMappers.kt`, `HugMappers.kt`, `PatternMappers.kt`.
- **Принципы:**
  - DTO → Domain: преобразование «сырых» значений в строгие доменные типы, enums, value‑классы (`UserId`, `HugId`, `PatternId`). Значения по умолчанию — по бизнес‑правилам.
  - Domain → DTO: для исходящих запросов (`POST/PATCH`) создаются `*RequestDto`/`*UpdateRequestDto`.

Пример (концептуально):

```kotlin
// DTO в :shared
@Serializable
data class HugDto(
    val id: String,
    @SerialName("from_user_id") val fromUserId: String,
    @SerialName("created_at") @Serializable(with = TimestampAsEpochMillisSerializer::class)
    val createdAt: Long
)

// Domain в :shared
data class Hug(
    val id: HugId,
    val fromUser: UserId,
    val createdAt: Long
)

// Mapper в :data:hugs
fun HugDto.toDomain(): Hug = Hug(
    id = HugId(id),
    fromUser = UserId(fromUserId),
    createdAt = createdAt
)
```

Полиморфные элементы `PatternElement` маппятся через централизованный конвертер в `:shared` (см. BLE/Pattern разделы в `01_ARCHITECTURE_OVERVIEW.md`).

---

### 4. Обработка ошибок (Error Handling)

- Каноника: `docs/50_CROSS_CUTTING_CONCERNS/01_ERROR_HANDLING.md`.
- Роль сети: все вызовы API проходят через `safeApiCall { ... }` (`:core:network`), который перехватывает `HttpException`, `IOException`, `SocketTimeoutException`, маппит их в `AppError` из `:shared` и логирует согласно `02_LOGGING_AND_TELEMETRY.md`.
- Маппинг HTTP:
  - 401 → `AppError.Unauthorized`
  - 403 → `AppError.Forbidden`
  - 404 → `AppError.NotFound`
  - 409 → `AppError.Conflict` или `AppError.VersionConflict(currentVersion)` (при оптимистической блокировке паттернов)
  - 412 → `AppError.PreconditionFailed(reason)`
  - 429 → `AppError.RateLimited`
  - 5xx → `AppError.Server(code, message)`
- Парсинг тела ошибки: через `JsonElement` без жёсткой схемы, извлекаем `message`, `errors`, `details` (см. примеры в `01_ERROR_HANDLING.md`).

---

### 5. Пагинация (Pagination)

- **Стратегия:** cursor‑based: `?cursor=...&limit=...` (см. OpenAPI v1). Ответы содержат `nextCursor`.
- **Интеграция с Paging 3:**
  - `RemoteMediator` в `:data:*` читает/пишет `remote_keys` (см. `01_DATABASE.md`).
  - На успешной загрузке страница upsert'ится в Room и в одной транзакции обновляется `remote_keys.nextCursor`.
  - Для разных срезов коллекции используются разные `partition` ключи (`sent|received`, фильтры и т.д.).

---

### 6. Контракты ApiService и соглашения

- Именование методов `ApiService`: глагол + ресурс, соответствующий бэкенду, например: `getHugs`, `sendHug`, `getPatterns`, `updatePattern`.
- Все методы `suspend` и используются только внутри `safeApiCall`.
- Все параметры дат и времени — миллисекунды эпохи (клиент), ISO‑строки (проводник) — конвертация строго в DTO‑сериализаторах.
- Для CUD операций поддерживаем идемпотентность посредством заголовка `Idempotency-Key` (если предусмотрено бэкендом); генерация ключей — в `:data:*` (см. `04_OFFLINE_SYNC.md`).

---

### 7. Безопасность и конфиденциальность

- Запрещено логировать PII и токены. В release отключено сетевое BODY‑логирование.
- Все запросы проходят с `Authorization: Bearer` и `X-Firebase-AppCheck`.
- Бинарные вложения/крупные payload'ы не передавать через DTO без явной необходимости; использовать ссылки и проверять размер.

---

### 8. Тестирование

- Интеграционные тесты репозиториев: `MockWebServer` + `Room(inMemory)`; проверка `RemoteMediator` и `remote_keys`.
- Контрактные тесты сериализации: round‑trip DTO по OpenAPI, особенно для `Timestamp` и полиморфных `PatternElement`.
- Тесты маппинга ошибок: соответствие кодов/тел JSON типам `AppError`.

---

### 9. Чек‑лист внедрения

- Создать/настроить `:core:network` с Hilt‑модулем, `OkHttpClient`, `Retrofit`, интерсепторами.
- В `:shared` определить DTO с `@Serializable`, сериализаторы `TimestampAsEpochMillisSerializer`, полиморфные сериализаторы для `PatternElement`.
- В `:data:*` реализовать `ApiService`, мапперы DTO↔Domain, `RemoteMediator` с транзакциями Room (`upsert + remote_keys`).
- Везде использовать `safeApiCall` и возвращать `Result<T, AppError>` (см. `01_ERROR_HANDLING.md`).
- Настроить `Paging 3` и кэширование по стратегии `read‑through + stale‑while‑revalidate`.

---

### Приложение A. Скелеты интерфейсов (ориентир)

```kotlin
interface HugsApiService {
    @GET("/v1/hugs")
    suspend fun getHugs(@Query("cursor") cursor: String?, @Query("limit") limit: Int): HugsPageDto

    @POST("/v1/hugs.send")
    suspend fun send(@Body body: SendHugRequestDto): HugDto
}

@Serializable
data class HugsPageDto(
    val items: List<HugDto>,
    @SerialName("next_cursor") val nextCursor: String?
)
```

```kotlin
suspend fun <T> safeApiCall(api: suspend () -> T): Result<T, AppError>
```

См. реализацию и подробности в `01_ERROR_HANDLING.md`.


