## ARCHITECTURE

### 1. Обзор (Overview)

Цель: дать быстрое, высокоуровневое представление об архитектуре Amulet App и экосистемы.

- Основной архитектурный паттерн: Clean Architecture с однонаправленным потоком данных и презентационным слоем в стиле MVVM с практиками MVI (MVVM+).
- Ключевые принципы:
  - Многомодульность и чёткие контрактные границы между слоями
  - Реактивность (Kotlin Flow), предсказуемый UDF
  - Разделение ответственностей и инверсия зависимостей
  - Тестируемость, детерминированные бизнес-правила
  - Offline-first, устойчивость к сетевым сбоям
  - Безопасность и приватность by design (App Check, токены Firebase, минимизация данных)

ASCII‑схема слоёв и потоков:

```
┌───────────────────────────────────────────────────────────────────────┐
│                            Presentation (Android)                     │
│  Jetpack Compose UI  ──>  ViewModel  ──>  StateFlow<ScreenState>     │
│         ▲                          │         ▲        │               │
│         │        SideEffects <─────┘         │        │               │
└─────────┼────────────────────────────────────┼────────┼───────────────┘
          │                                     │        │
          ▼                                     ▼        │
┌─────────────────────────────────────────────────────────┼──────────────┐
│                             Domain (KMP shared)         │              │
│  UI Action → UseCase → Repository (interface) → Policies│              │
│                     ▲                                   │              │
└─────────────────────┼───────────────────────────────────┘              │
                      │                                                  │
                      ▼                                                  ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                                 Data (Android)                              │
│  Repo impl → Local (Room/Prefs) ↔ Cache ↔ Remote (Retrofit/OkHttp)         │
│                 BLE/NFC, OTA, Telemetry, Notifications (FCM)                │
└────────────────────────────────────────────────────────────────────────────┘
```


### 2. Технологический стек (Tech Stack)

Обоснованный выбор ключевых технологий:

- Язык: Kotlin (JVM), готовность к KMP для `:shared`.
- UI: Jetpack Compose (declarative UI, анимации, state hoisting, тестируемость).
- Асинхронность: Kotlin Coroutines & Flow (структурированная конкуррентность, backpressure, cancellation).
- Архитектурные компоненты: Jetpack ViewModel, Navigation Compose (type‑safe граф, deep link/NFC‑entry).
- DI: Hilt (Android) + возможность Koin в `:shared` (KMP‑friendly). На Android уровне Hilt связывает реализации с интерфейсами из `:shared`.
- Сеть: Retrofit + OkHttp (конвейер интерсепторов: аутентификация Firebase ID Token, App Check, retry/backoff, logging в dev).
- Сериализация: kotlinx.serialization (KMP, стабильные модели DTO/конвертеры).
- Локальное хранилище: Room (SQL, миграции, Paging), DataStore (преференсы/ключ‑значение).
- Тестирование: JUnit5, MockK, Turbine (Flow), Compose UI Test, Robolectric/Instrumented, Kotest (BDD опционально).
- Работа с железом: Android BLE API (GATT), Android NFC API (NDEF/Deep Link), Foreground Service для устойчивых сессий.
- Уведомления: Firebase Cloud Messaging (FCM), топики/токены (см. `/notifications.tokens`).
- OTA: клиент к эндпоинтам `/ota/*`, верификация checksum, прогресс‑репорт `/devices/{deviceId}/firmware/report`.
- Аналитика/Телеметрия: клиент к `/telemetry/events`, локальные очереди/батчинг.
- CI/CD: GitHub Actions (сборка, линтинг, тесты, подпись, деплой в Firebase App Distribution/Test Lab).


### 3. Стратегия модульности (Modularization Strategy)

Философия: многомодульность снижает время сборки, изолирует ответственность, позволяет переиспользование и независимую эволюцию фич.

Типы модулей (планируемая структура):

- `:app` — Android entrypoint, DI‑композиция, навигация, разрешения.
- `:shared` — KMP Domain: use case'ы, модели, интерфейсы репозиториев, бизнес‑правила.
- `:core:network` — Retrofit/OkHttp, авторизация, App Check, сериализация.
- `:core:database` — Room/DAO, миграции, DataStore.
- `:core:ble` — BLE/NFC абстракции, GATT‑профили, конверсия паттернов → команды.
- `:core:telemetry` — сбор, батчинг, отправка телеметрии.
- `:core:design` — Compose design system (themes, typography, components).
- `:data:user` — реализация `UserRepository` (профиль, настройки, консенсы).
- `:data:devices` — реализация `DevicesRepository` (привязка, OTA, статус).
- `:data:hugs` — реализация `HugsRepository` («объятия», пары, история).
- `:data:practices` — реализация `PracticesRepository` (каталог, сессии, статистика).
- `:data:patterns` — реализация `PatternsRepository` (CRUD, модерация, preview).
- `:data:rules` — реализация `RulesRepository` (триггеры, интеграции, вебхуки).
- `:data:privacy` — реализация `PrivacyRepository` (экспорт, удаление, аудит).
- `:feature:dashboard` — экран состояния устройства/быстрых действий.
- `:feature:library` — каталог практик `/practices`.
- `:feature:hugs` — «объятия» `/hugs`, пары `/pairs`.
- `:feature:patterns` — редактор/каталог паттернов `/patterns`.
- `:feature:sessions` — запуск/стоп сессий практик `/practices.*`.
- `:feature:devices` — привязка/управление устройствами `/devices.*`.
- `:feature:settings` — профиль, уведомления, приватность `/privacy.*`.

Правила зависимостей:

- `:feature:*` → зависит от `:shared`, `:core:*`, `:core:design` и соответствующего `:data:*` модуля. Запрещены прямые зависимости `feature ↔ feature`.
- `:data:*` → зависит от `:shared` (интерфейсы, DTO), `:core:network`, `:core:database`, `:core:ble` (по необходимости). Не зависит от `:feature:*`.
- `:shared` не зависит от Android/Compose/Retrofit/Room. Только Kotlin stdlib, kotlinx.serialization, Koin (опционально).
- `:core:*` могут зависеть от Android SDK, но не от `:feature:*` или `:data:*`.
- Направление: Presentation → Domain → Data (через интерфейсы). Инверсия: реализации регистрируются DI на Android слое.


Вариант повышенной изоляции (идеалистический для эталонного приложения):

Идея — `:feature:*` зависят только от `:shared` и `:core:design`. Реализации репозиториев из `:data:*` подключаются на уровне `:app` через DI (Hilt), а фичи получают лишь интерфейсы.

- Обновлённые правила зависимостей (строгая изоляция):
  - `:feature:*` → зависит от `:shared`, `:core:design` (без прямой зависимости от `:data:*` и `:core:*`).
  - `:data:*` → зависит от `:shared`, `:core:network`, `:core:database`, `:core:ble` (по необходимости). Не зависит от `:feature:*`.
  - `:app` → единственное место, где сводятся `:feature:*` + `:data:*` (через Hilt‑модули биндингов).

Преимущества:

- Ещё быстрее инкрементальные сборки: изменение в `:data:*` не триггерит пересборку `:feature:*`.
- Лучшая тестируемость фич: легко подменять репозитории моками через DI без зависимостей на реализацию.
- Максимальная независимость фич от источников данных (сеть/БД/моки).

Диаграмма (строгая изоляция):

```
:app
 ├─ :feature:* ──┐        (интерфейсы из :shared)
 │               └─ :shared
 ├─ :data:* ──────┐
 │                ├─ :shared (interfaces, DTO)
 │                ├─ :core:network
 │                ├─ :core:database
 │                └─ :core:ble (if needed)
 └─ DI: bind(HugsRepositoryImpl as HugsRepository), ...
```

Пример биндинга (в `:app`):

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindingsModule {
    @Binds abstract fun bindHugs(impl: HugsRepositoryImpl): HugsRepository
    @Binds abstract fun bindDevices(impl: DevicesRepositoryImpl): DevicesRepository
    // ... другие биндинги
}
```

**Преимущества data-слоя:**

- **Изоляция ответственности:** Модуль `:data:hugs` знает только о том, как работать с «объятиями». Он зависит от `:core:network` (Retrofit-клиент), `:core:database` (DAO) и `:shared` (интерфейс `HugsRepository`, DTO-модели).
- **Чёткие зависимости:** Фича-модуль `:feature:hugs` зависит от `:data:hugs`, чтобы Hilt мог предоставить `HugsRepositoryImpl` для `SendHugUseCase`.
- **Масштабируемость:** Добавление новой сущности (например, «Достижения») требует только создания интерфейса в `:shared`, DTO/DAO в `:core:*` и реализации в новом модуле `:data:achievements`.

**Пример структуры `:data:hugs` модуля:**
```
:data:hugs/
├── src/main/java/
│   └── com/example/amulet/data/hugs/
│       ├── HugsRepositoryImpl.kt
│       ├── HugsApiService.kt
│       ├── HugsDao.kt
│       ├── HugsMapper.kt
│       └── HugsCachePolicy.kt
└── build.gradle.kts
```

**DI-регистрация в `:app`:**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindHugsRepository(impl: HugsRepositoryImpl): HugsRepository
    
    @Binds
    abstract fun bindDevicesRepository(impl: DevicesRepositoryImpl): DevicesRepository
    
    // ... другие репозитории
}
```

Навигация между фичами осуществляется через type‑safe роуты Navigation Compose, определённые в модуле `:app`. Каждая `:feature:*` предоставляет свой навигационный граф/entry‑destinations (и аргументы) как расширения навигации, которые регистрируются в `:app` через DI/функции‑поставщики.


**Абстракция над BLE (`:core:ble`)**

Работа с Bluetooth Low Energy на Android сложна (различия устройств/прошивок, особенности стека, разрывы связи). Абстракция в `:core:ble` должна быть железобетонной и предоставлять стабильный, реактивный контракт:

- Реактивный API:
  - `Flow<ConnectionState>` — состояния подключения (Disconnected, Connecting, Connected, ServicesDiscovered, Reconnecting, Failed(cause)).
  - `Flow<Int>` для метрик (например, `BatteryLevel`), `Flow<DeviceStatus>` для агрегированных статусов.
  - Горячие потоки с replay где нужно, backpressure‑безопасность.
- Управление жизненным циклом и отказами:
  - Встроенные политики переподключения с экспоненциальным бэкофом и джиттером.
  - Таймауты на операции GATT (connect, discover, read/write, notify/indicate), перевод в чёткие доменные ошибки.
  - Авто‑повтор безопасных идемпотентных операций.
- Очередь команд:
  - Сериализация write/read запросов (single in‑flight), гарантии порядка, отмена по контексту корутин.
  - Приоритеты (высокий для критичных команд, низкий для фоновых), коалесинг схожих команд.
  - Буферизация при кратковременном оффлайне и сброс по политике времени/объёма.
- Инкапсуляция специфик:
  - UUID сервисов/характеристик, MTU negotiation, настройки PHY, включение уведомлений/индикаций.
  - Конвертация высокоуровневых паттернов и жестов в низкоуровневые команды (см. конвертер из `:core:ble`).

Пример контракта (упрощённо):

```kotlin
interface AmuletBleClient {
    val connectionState: StateFlow<ConnectionState>
    val batteryLevel: Flow<Int>

    suspend fun connect(deviceId: String, autoReconnect: Boolean = true)
    suspend fun disconnect()

    suspend fun sendCommand(command: AmuletCommand): BleResult
    fun observeNotifications(type: NotificationType): Flow<ByteArray>
}

sealed interface ConnectionState {
    data object Disconnected : ConnectionState
    data object Connecting : ConnectionState
    data object Connected : ConnectionState
    data object ServicesDiscovered : ConnectionState
    data class Reconnecting(val attempt: Int) : ConnectionState
    data class Failed(val cause: Throwable?) : ConnectionState
}
```

Внутри клиента — отдельная корутина‑воркер, обрабатывающая очередь команд с гарантиями последовательности и таймаутами; все ошибки GATT маппятся в доменные ошибки `AppError.Network/Timeout/PreconditionFailed` по политике.


**Конвертер Pattern → BLE‑команды**

OpenAPI определяет богатую модель `PatternSpec`/`PatternElement*` (градиенты, пульс, дыхание и пр.). Для исполнения на устройстве требуется трансляция в компактные wire‑команды (строки/байты), например `BREATHING:00FF00:8000ms` или бинарный формат.

- Размещение:
  - Ядро преобразования (pure function) — в `:shared` (KMP): `PatternSpec` → `DeviceCommandPlan` (абстрактное представление последовательности команд, не привязанное к BLE/HTTP). Это обеспечивает переиспользование на iOS.
  - Транспортный адаптер — в `:core:ble`: `DeviceCommandPlan` → `ByteArray`/`List<ByteArray>` с учётом MTU/PHY, чанкинга, CRC/Checksum, префиксов сервиса/характеристик.
- Контракты:
```kotlin
// :shared
interface PatternCompiler {
    fun compile(spec: PatternSpec, hardwareVersion: Int, firmwareVersion: String): DeviceCommandPlan
}

data class DeviceCommandPlan(
    val commands: List<DeviceCommand>,
    val estimatedDurationMs: Long
)

sealed interface DeviceCommand {
    data class Breathing(val color: Rgb, val durationMs: Int) : DeviceCommand
    data class Pulse(val color: Rgb, val speed: Int, val repeats: Int) : DeviceCommand
    // ... другие высокоуровневые команды
}

// :core:ble
interface BleCommandEncoder {
    fun encode(plan: DeviceCommandPlan, mtu: Int): List<ByteArray>
}
```

- Правила компиляции:
  - Учитывать `hardwareVersion` (100 — 1 LED, 200 — кольцо 12 LED): деградация/апгрейд эффектов.
  - Учитывать `firmwareVersion` (например, `2.1.0`): поддержка новых команд (например, `Sparkle`), обратная совместимость, миграция устаревших команд.
  - Нормализация параметров (скорости, интенсивности) в допустимые диапазоны прошивки.
  - Валидация и фолбэки для неподдерживаемых `PatternElement`.
- Отладка и тестирование:
  - Snapshot‑тесты компилятора (входной `PatternSpec` → стабильный `DeviceCommandPlan`).
  - Golden‑тесты кодировщика BLE (план → bytes) и проверка MTU‑чанкинга.
  - Превью в UI использует тот же компилятор для консистентности визуализации и реального вывода.

**Версионирование команд:**

- Команды версионируются по `firmwareVersion` (семантическое версионирование). Компилятор выбирает подходящий набор команд и их параметры.
- Примеры:
  - `firmwareVersion < 2.1.0` — `Sparkle` не поддерживается, заменяется на `Pulse` с fallback‑параметрами.
  - `firmwareVersion >= 2.1.0` — доступны новые команды, расширенные параметры (например, `Sparkle(count, color, spread)`).
- Регистр команд: в `:shared` хранится мапа `firmwareVersion → Set<CommandType>`, компилятор проверяет поддержку перед генерацией.

**Атомарность и транзакции:**

- Проблема: разрыв соединения во время отправки `DeviceCommandPlan` из N команд может оставить устройство в промежуточном состоянии.
- Решение — транзакционная отправка:
  - `BleCommandEncoder` оборачивает план в транзакцию: `BEGIN_TRANSACTION` → команды → `COMMIT_TRANSACTION` (или `ROLLBACK` при ошибке).
  - Прошивка буферизует команды до `COMMIT`; при разрыве до `COMMIT` — автоматический откат.
  - Таймаут транзакции: если `COMMIT` не пришёл в течение T, прошивка откатывает изменения.
- Альтернатива — чанкинг с подтверждениями:
  - Разбивка большого плана на чанки, подтверждение каждого чанка устройством.
  - При потере подтверждения — повтор отправки чанка или откат всей анимации.
- UI‑обратная связь:
  - Статус «Загружается анимация…» с прогрессом (чанки/команды).
  - При ошибке — возможность повтора с того же чанка или отмены.

**Стратегия разрешения конфликтов для редактора паттернов:**

Проблема: политика «server wins» может уничтожить 20 минут работы пользователя при редактировании паттерна в офлайне.

**Оптимистическая блокировка + Разрешение конфликта пользователем:**

- **Бэкенд:**
  - В модель `Pattern` добавлено поле `version: Int` (атомарно инкрементируется при каждом `PATCH`).
  - `PATCH /patterns/{id}` требует заголовок `If-Match: version` или параметр `editingVersion`.
  - Логика: `ЕСЛИ editingVersion == currentVersion, ТО обновить и version++, ИНАЧЕ 409 Conflict`.
- **Клиент:**
  - При открытии паттерна сохраняется `editingVersion`.
  - При сохранении отправляется `editingVersion` в запросе.
  - При `409 Conflict` — показ диалога разрешения конфликта с опциями:
    - «Сохранить мою версию» (перезаписать серверную)
    - «Использовать новую с сервера» (отменить локальные изменения)
    - «Сохранить как копию» (создать новый паттерн с локальными правками)
    - «Отмена» (вернуться к редактированию)

**Альтернативы:**

- **Трёхстороннее слияние:** автоматическое объединение изменений через `diff3`-алгоритм. Сложно в реализации для JSON-структур, избыточно для редких одновременных правок.
- **Операционные трансформации:** для real-time коллаборации. Избыточно для асинхронного редактирования.

**Реализация в архитектуре:**

- `:data:patterns` содержит логику обнаружения конфликтов и UI-состояния для диалогов.
- `:feature:patterns` обрабатывает пользовательский выбор в диалоге конфликта.
- `:core:network` поддерживает заголовки `If-Match`/`If-None-Match` для оптимистической блокировки.

**Пример реализации:**

```kotlin
// :shared
data class Pattern(
    val id: String,
    val version: Int, // для optimistic locking
    val spec: PatternSpec,
    // ... другие поля
)

// :data:patterns
class PatternsRepositoryImpl {
    suspend fun updatePattern(
        id: String, 
        editingVersion: Int, 
        updates: PatternUpdateRequest
    ): Result<Pattern, AppError> {
        return try {
            val response = apiService.updatePattern(id, editingVersion, updates)
            Result.success(response.pattern)
        } catch (e: HttpException) {
            when (e.code()) {
                409 -> Result.failure(AppError.Conflict("Pattern was modified by another user"))
                else -> Result.failure(AppError.Server(e.code(), e.message()))
            }
        }
    }
}

// :feature:patterns
class PatternEditViewModel {
    private var editingVersion: Int = 0
    
    fun startEditing(pattern: Pattern) {
        editingVersion = pattern.version
        // ... инициализация UI
    }
    
    fun savePattern() {
        viewModelScope.launch {
            when (val result = patternsRepository.updatePattern(patternId, editingVersion, updates)) {
                is Result.Success -> {
                    // Обновить UI, показать успех
                }
                is Result.Failure -> when (result.error) {
                    is AppError.Conflict -> {
                        // Показать диалог разрешения конфликта
                        showConflictResolutionDialog()
                    }
                    else -> {
                        // Показать общую ошибку
                    }
                }
            }
        }
    }
}
```


### 4. Потоки данных и управление состоянием (Data Flow & State Management)

Однонаправленный поток данных (UDF):

1) UI Action (Intent) → 2) ViewModel (обработка) → 3) UseCase → 4) Repository → 5) Remote/Local/BLE → 6) Result → 7) UseCase policy → 8) ViewModel редьюсер → 9) `StateFlow<ScreenState>` → UI.

Моделирование состояния UI:

- Каждый экран имеет `data class <Screen>State` с полями: данные, флаги загрузки, ошибки, ephemeral‑состояния.
- ViewModel предоставляет один `StateFlow<ScreenState>` и, при необходимости, `SharedFlow<SideEffect>` для одноразовых событий (навигация, тост, snackbar).
- Сайд‑эффекты не сериализуются в состояние, доставляются отдельно (replay=0).

Редьюсеры и интенты:

- Интенты пользователя (например, `OnSendHug`, `OnStartPractice`) маппятся в события, редьюсер обновляет `ScreenState` иммутабельно.
- Политики повторов и дебаунс (Flow operators) реализуются в ViewModel/UseCase.

Кэш и консистентность:

- Репозитории объединяют источники: Local first → Remote/Sync → Reconcile. Конфликты разрешаются по политике «server wins», с возможностью мерджа для паттернов.
- Для списков (история «объятий», практики) — Paging + локальные ключи курсоров (`nextCursor`).

Offline‑first для команд (исходящих действий):

- Принцип: помимо чтения из кэша, исходящие действия (например, отправка «объятия», приглашение пары, старт/стоп сессии) должны быть устойчивыми к отсутствию сети.
- UX‑ожидание: пользователь видит статус «Отправляется…» и может продолжать работу; действие доотправится автоматически при появлении сети.
- Очередь исходящих действий:
  - Отдельная таблица в Room, например `outbox_actions`, с полями: `id`, `type` (e.g. `SEND_HUG`), `payloadJson`, `createdAt`, `attempts`, `lastError`, `status` (`pending`, `in_flight`, `failed`, `completed`), `idempotencyKey`.
  - Все команды пишутся транзакционно: обновление локального UI‑состояния + запись в `outbox_actions`.
  - Для BLE‑команд возможна отдельная подочередь с приоритетом/таймаутами (см. `:core:ble`).
- Диспетчеризация:
  - WorkManager (констрейнты: «сеть доступна», «устройство на питании» опционально) периодически пытается отправить pending‑элементы, соблюдая backoff.
  - Для команд к HTTP API — использование идемпотентных ключей (заголовок/параметр) и повторов с экспоненциальным бэкофом.
  - Для команд к BLE — делегирование в очередь `:core:ble` с подтверждением доставки.
- Консистентность и подтверждение:
  - При успешном ответе — помечаем `completed`, выполняем reconcile (например, добавляем «объятие» в локальную историю, если сервер вернул ID), очищаем `lastError`.
  - При ошибках 4xx/бизнес‑ошибках — переводим в `failed` с причиной и отображаем пользователю side‑effect (toast/snackbar) + CTA для ретрая/отмены.
  - При 5xx/сети — оставляем в `pending`/`in_flight` согласно политике повторов.
- Пример статусов для «объятия» в UI:
  - `queued` → «Отправится при подключении»
  - `sending` → «Отправляется…»
  - `sent`/`delivered` → «Доставлено» (на основе ответа API)
  - `failed` → кнопка «Повторить» (тригерит re‑enqueue)
- Наблюдение:
  - ViewModel подписывается на Flow из DAO `outbox_actions` по типам, маппит их в UI state для соответствующих списков/деталей.
  - Telemetry фиксирует успех/неудачи и метрики задержек (время от enqueue до completed).

**Трёхслойная архитектура моделей:**

В приложении используются три типа моделей, каждая для своего слоя:

**а) DTO (Data Transfer Object) — Сетевой слой**

- **Где живут:** `:shared` модуль (так как они являются частью контракта с API и нужны для KMP).
- **Назначение:** Точное представление JSON-ответов от вашего API (согласно `openapi_v1_бэкенда.yaml`). Они содержат аннотации `@Serializable` и могут иметь поля, специфичные для API (например, `_seconds` для Timestamp).

**б) Сущности БД (Database Entities) — Слой базы данных**

- **Где живут:** `:core:database` модуль.
- **Назначение:** Представление таблицы в базе данных Room. Содержат аннотации `@Entity`, `@PrimaryKey` и т.д. Структура этих классов оптимизирована для хранения и запросов в SQL.

**в) Доменные модели (Domain Models) — Самое важное!**

- **Где живут:** `:shared` модуль, в `domain` пакете.
- **Назначение:** Чистое, идеальное представление бизнес-сущности в вашем приложении. Это модели, с которыми работают UseCase'ы и которые ViewModel использует для формирования своего состояния. Они не содержат никаких аннотаций, связанных с фреймворками (ни `@Serializable`, ни `@Entity`). Это простые `data class`.

**Как это всё работает вместе? С помощью мапперов.**

Каждый `:data:*` модуль содержит мапперы для конверсии между слоями


**Поток данных через слои:**

1. **API → DTO:** Retrofit автоматически десериализует JSON в DTO
2. **DTO → Domain:** Маппер в репозитории конвертирует DTO в доменную модель
3. **Domain → Entity:** При сохранении в БД маппер конвертирует доменную модель в Entity
4. **Entity → Domain:** При чтении из БД маппер конвертирует Entity обратно в доменную модель
5. **Domain → UI State:** ViewModel использует доменные модели для формирования `ScreenState`


### 5. Кросс‑платформенная стратегия (Kotlin Multiplatform Strategy)

Роль `:shared`:

- Домейн‑слой: интерфейсы репозиториев, UseCase, бизнес‑правила, модели/DTO (kotlinx.serialization) и мапперы.
- Портируемость: iOS/other платформы смогут переиспользовать домейн без Android‑зависимостей.

Что остаётся на платформе (Android):

- Реализации репозиториев (сеть `Retrofit`, БД `Room`, BLE/NFC), все адаптеры инфраструктуры, UI (Compose), DI‑wireup (Hilt), уведомления/сервисы.

Связывание:

- Android внедряет реализации для интерфейсов из `:shared` через DI. Для KMP возможен Koin в `:shared` с абстрактными модулями и Hilt‑мостом на Android.


### 6. Обработка ошибок (Error Handling)

Классификация ошибок (Domain):

```kotlin
sealed interface AppError {
  data object Unauthorized : AppError
  data object Forbidden : AppError
  data object NotFound : AppError
  data class Validation(val field: String?, val message: String?) : AppError
  data object RateLimited : AppError
  data object Network : AppError
  data object Timeout : AppError
  data object Conflict : AppError
  data object PreconditionFailed : AppError
  data class Server(val code: Int, val message: String?) : AppError
  data object Unknown : AppError
}
```

Путь ошибки:

- Data слой перехватывает исключения/коды HTTP и маппит на `AppError`. Бизнес‑UseCase при необходимости нормализует в доменные исходы (`Either<AppError, Result>` или `Result<T, AppError>`).
- Вьюмодель хранит `error: AppError?` в `ScreenState` или доставляет через `SideEffect`.

Отображение на UI:

- Некритичные — snackbar/toast; критичные — полноэкранные состояния (Empty/Error), CTA для ретрая.
- Rate limit (`429`) — информирование о кулдауне (см. `/hugs.send` и `Retry-After`).


### 7. Тестирование (Testing)

Пирамида тестирования и размещение:

- Unit (основа): UseCase и редьюсеры (Turbine для Flow, MockK для репо).
- Интеграционные: Repo + Network/DB (in‑memory Room, MockWebServer, OkHttp idling).
- Контрактные: проверки соответствия OpenAPI моделям/конвертерам (serialization round‑trip, codegen опционально).
- UI: Compose Test (state → UI, семантические ноды, скриншоты), навигация.
- E2E/Instrumented: онбординг, привязка устройства (mock BLE), «объятия» happy‑path.

Нефункциональные проверки:

- Производительность (startup time, jank, frame time), стабильность фоновых сервисов.
- Надёжность офлайн‑режима и восстановления сессий.
- Безопасность (Scoped Storage, токены, App Check).


### Приложение концепции к API (выдержки OpenAPI v1)

- Пользователи/профиль: `/users.me*` — профиль, локализация, consents.
- Устройства: `/devices*` — привязка/отвязка, настройки, статус, отчёты OTA.
- «Объятия»: `/hugs*`, Пары: `/pairs*` — отправка, история, блокировки.
- Библиотека: `/practices*`, Паттерны: `/patterns*` — каталог/CRUD/модерация/preview.
- Сессии: `/practices.start|stop`, агрегаты: `/stats/overview`.
- Правила/интеграции: `/rules*`, `/webhooks/*` — триггеры, IFTTT‑подобные сценарии.
- Приватность: `/privacy/*` — экспорт/удаление/права/аудит.
- Админ: `/admin/*` — модерация, ролевая модель, аудит секретов вебхуков.


### Важные архитектурные решения (ADR‑уровень)

- Auth: Firebase ID Token (Bearer), App Check обязателен для мобильных клиентов. Токены освежаются через Firebase SDK; интерсепторы прикладывают заголовки к каждому запросу.
- Offline‑first: Room как источник истины для кэшируемых данных; стратегии «stale‑while‑revalidate», курсоры (`nextCursor`) для пагинации.
- Гибридные каналы устройства: BLE для команд реального времени, HTTPS для долгоживущих операций и синхронизаций, FCM для пуш‑сигналов.
- OTA: проверка `/ota/firmware/latest` с `hardware` и `currentFirmware`, последующий отчёт `/devices/{deviceId}/firmware/report`.
- Конструктор анимаций: модели паттернов совместимы со схемами `/components/schemas/Pattern*`; адаптация по `hardwareVersion` (100/200). Компилятор `PatternSpec → DeviceCommandPlan` расположен в `:shared`; кодировщик wire‑формата и MTU‑чанкинг в `:core:ble`.
- Телеметрия: батч‑отправка `/telemetry/events` с бэкофом; частичная устойчивость при офлайне через локальные очереди.
- Приватность: единый поток удаления/экспорта аккаунта через `/privacy/*`; UI предоставляет понятные статусы и сроки готовности.


### Нефункциональные требования (SLO/SLA ориентиры)

- Startup (P50): ≤ 800 мс до первого meaningful render; (P95) ≤ 1500 мс на mid‑range.
- Навигация между экранами: (P95) ≤ 300 мс.
- Отправка «объятия» до подтверждения: (P95) ≤ 1.5 с (сеть + пуш).
- OTA: время проверки обновления ≤ 400 мс (сеть), UI прогресс/ретраи.
- Crash‑free sessions: ≥ 99.7%.


### Безопасность и соответствие

- Минимизация собираемых данных; принцип наименьших привилегий.
- Сетевые запросы: TLS, проверка App Check; устойчивые к повторам идемпотентные операции.
- Хранение секретов: токены FCM в защищённом хранилище; не логируются PII.
- Ротация секретов вебхуков на бэкенде поддерживается эндпоинтами `/admin/webhook-secrets/*`.


### Руководство по внесению изменений

- Любое изменение API/схем — синхронизировать модели `:shared` и конвертеры `:core:network`.
- Новая фича — отдельный `:feature:*` модуль, публичные контракты в `:shared`.
- Диаграмма зависимостей проверяется static‑линтером (Forbidden APIs/deps rules).
- Добавляйте тесты: unit для UseCase/редьюсеров, интеграционные для источников данных, UI для основных сценариев.


### Глоссарий

- ScreenState — неизменяемая модель состояния экрана, источник истины для UI.
- SideEffect — одноразовое событие (навигация, тост), не часть сохранённого состояния.
- UseCase — атомарная бизнес‑операция (напр., SendHugUseCase).
- Repository — фасад к данным (Remote/Local/BLE), инкапсулирующий источники и политики консистентности.


