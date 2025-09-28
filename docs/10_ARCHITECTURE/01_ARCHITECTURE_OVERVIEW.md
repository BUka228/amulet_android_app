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


### Стратегия синхронизации данных в реальном времени (Real‑Time)

Задача: мгновенная доставка событий (например, входящее «объятие») без периодического опроса API.

- Канал: Firebase Cloud Messaging (FCM) — регистрация токена уже предусмотрена (`/notifications.tokens`). Используются data‑messages.
- Поток обработки:
  1. Бэкенд после успешного `hugs.send` отправляет FCM data‑message получателю: `{ "type": "hug_received", "hugId": "…" }`.
  2. `FirebaseMessagingService` на клиенте получает сообщение даже в фоне/killed (в рамках ограничений ОС) и маршрутизует по типу.
  3. Data слой: вызывается соответствующий метод репозитория, например `HugsRepository.onHugReceived(hugId, payload)`:
     - Загружает детали по `hugId` (если payload неполный) или десериализует из payload.
     - Сохраняет в Room (upsert) с дедупликацией по первичному ключу.
  4. UI: `Flow` из DAO автоматически обновляет экраны (история «объятий»), если они активны.
  5. BLE: если устройство подключено и пользователь разрешил, слой `:core:ble` отправляет команду на амулет для немедленной анимации.

- Политики и состояния приложения:
  - Foreground: обрабатываем data‑message, обновляем БД, показываем мягкий UI‑сигнал (in‑app banner/snackbar), анимация на амулете по настройкам.
  - Background: показываем системное уведомление (notification) с навигацией на соответствующий экран; БД обновляется в фоне.
  - Killed: FCM может доставить уведомление; при запуске приложения триггерим фоновую синхронизацию непрочитанных событий (best‑effort reconcile).

- Надёжность:
  - Дедупликация по `messageId`/`hugId` в БД (уникальные ключи, upsert).
  - Ретраи: при частичной недоставке payload — догружаем детали по API с бэкофом.
  - Порядок: события идемпотентны; UI основан на времени создания/состоянии, не на очередности доставок.

- Приватность и согласия:
  - Перед триггером анимации и записью телеметрии проверяются согласия пользователя (`User.consents`).
  - Уведомления/каналы — в соответствии с настройками уведомлений пользователя.

- Безопасность:
  - Токены FCM регистрируются/отзываются через `/notifications.tokens`.
  - Data‑messages валидируются (подписи/ожидаемые поля), игнорируются неизвестные типы.

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
- `:core:config` — удалённая конфигурация и фича‑флаги (Firebase Remote Config, кэш/дефолты).
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

 Правила зависимостей (строгая изоляция — выбранный подход):

- `:feature:*` → зависит только от `:shared`, `:core:design`. Запрещены прямые зависимости `feature ↔ feature` и зависимости на `:data:*`/`:core:*`.
- `:data:*` → зависит от `:shared` (интерфейсы, DTO), `:core:network`, `:core:database`, `:core:ble` (по необходимости). Не зависит от `:feature:*`.
- `:shared` не зависит от Android/Compose/Retrofit/Room. Только Kotlin stdlib, kotlinx.serialization, Koin (опционально).
- `:core:*` могут зависеть от Android SDK, но не от `:feature:*` или `:data:*`.
- Направление: Presentation → Domain → Data (через интерфейсы). Инверсия: реализации регистрируются DI на уровне `:app`.


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

**Преимущества data-слоя (при строгой изоляции фич):**

- **Изоляция ответственности:** Модуль `:data:hugs` знает только о том, как работать с «объятиями». Он зависит от `:core:network` (Retrofit-клиент), `:core:database` (DAO) и `:shared` (интерфейс `HugsRepository`, DTO-модели).
- **Чёткие зависимости:** Фича‑модуль `:feature:hugs` зависит только от `:shared`; привязка `HugsRepositoryImpl` к `HugsRepository` выполняется в `:app` через DI (Hilt/Koin‑мост), поэтому изменения в `:data:hugs` не пересобирают `:feature:hugs`.
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


### Управление жизненным циклом BLE (Foreground Service)

Требование: BLE‑соединение и сессии (например, медитация) должны переживать уничтожение UI и сворачивание приложения. Для этого используется Foreground Service, владеющий `AmuletBleClient` и отображающий постоянное уведомление.

- Поток запуска:
  1. Пользователь запускает практику/операцию, требующую стабильного канала.
  2. ViewModel/UseCase вызывает `StartBleForegroundServiceUseCase`.
  3. ForegroundService стартует, поднимает уведомление и инициализирует/получает экземпляр `AmuletBleClient` из DI.
  4. Сервис подписывается на `connectionState`, управляет переподключениями и выполняет команды (в т.ч. загрузки анимаций через `upload(plan)`).
  5. UI‑экраны биндятся к сервису для получения `Flow`‑состояний и отправки команд, но сервис не зависит от присутствия UI.

- Взаимодействия (упрощённо):

```
UI (Compose/ViewModel)
   │ start/stop, commands, observe progress
   ▼
ForegroundService (lifecycle owner of BLE)
   │ holds
   ▼
AmuletBleClient (:core:ble)
   │ emits state, executes queue
   ▼
Repositories (:data:devices/:data:sessions) ↔ Room/Network
```

- Принципы:
  - Единственная точка владения BLE — сервис. ViewModel никогда не создаёт BLE‑клиента.
  - Сервис предоставляет binder/IPC‑фасад с методами: `observeConnection()`, `observeUpload()`, `sendCommand()`, `startSession()`, `stopSession()`.
  - Безопасное восстановление после process‑death: при рестарте сервис перечитывает контекст активной сессии из БД/DataStore и восстанавливает состояние.
  - Разрешения/фоновые ограничения: сервис использует Foreground‑ограничения Android (channel, importance), уважает энергополитику, корректно останавливается.

- Политика запуска/остановки (энергоэффективность):
  - Сервис стартует ТОЛЬКО для активных сценариев: запущенная практика/сессия, OTA‑обновление, тест/предпросмотр паттерна, критические фоновые операции, требующие стабильного канала.
  - Для «объятий» в фоне/килл‑состоянии: используется «FCM push → запуск сервиса → подключение → воспроизведение анимации → авто‑остановка через N секунд/по завершению».
  - Сервис НЕ поддерживает постоянное фоновое соединение «на всякий случай» — это дорого по батарее и ненадёжно из‑за ограничений ОС.
  - Грейс‑период: после сворачивания UI сервис может держать соединение короткое время (конфигурируемо, например 15–30 сек) для догрузки/доигрывания, затем отключается.
  - Авто‑остановка: при отсутствии активных задач/команд и по таймауту сервис сам останавливается (`stopForeground(true)` / `stopSelf()`).

**Конвертер Pattern → BLE‑команды**

OpenAPI определяет богатую модель `PatternSpec`/`PatternElement*` (градиенты, пульс, дыхание, последовательности и пр.). Для исполнения на устройстве требуется трансляция в компактные wire‑команды (строки/байты), например `BREATHING:00FF00:8000ms` или бинарный формат.

**Поддержка последовательностей команд (PatternElementSequence):**

Для реализации "секретных кодов" и других дискретных последовательностей добавлен специализированный тип элемента `PatternElementSequence`. Этот элемент позволяет описывать точные последовательности действий:

- **LedAction**: Включение конкретного светодиода (или всех) определенным цветом на заданное время
- **DelayAction**: Простые паузы между действиями

Пример "секретного кода" "Я скучаю" (двойная пульсация верхнего диода, потом одинарная нижнего):

```json
{
  "type": "sequence",
  "params": {
    "steps": [
      { "type": "led", "ledIndex": 0, "color": "#FF00FF", "durationMs": 150 },
      { "type": "delay", "durationMs": 100 },
      { "type": "led", "ledIndex": 0, "color": "#FF00FF", "durationMs": 150 },
      { "type": "delay", "durationMs": 400 },
      { "type": "led", "ledIndex": 4, "color": "#FFFF00", "durationMs": 200 }
    ]
  }
}
```

Этот подход обеспечивает:
- **Эффективность**: Компактное JSON-представление без избыточности
- **Простота компиляции**: Прямое соответствие между `SequenceStep` и BLE-командами
- **Читаемость**: Понятная структура для UI и отладки

- Размещение:
  - Ядро преобразования (pure function) — в `:shared` (KMP): `PatternSpec` → `DeviceCommandPlan` (абстрактное представление последовательности команд, не привязанное к BLE/HTTP). Это обеспечивает переиспользование на iOS.
  - Транспортный адаптер — в `:core:ble`: `DeviceCommandPlan` → `ByteArray`/`List<ByteArray>` с учётом MTU/PHY, чанкинга, CRC/Checksum, префиксов сервиса/характеристик.
- Контракты:
```kotlin
// :shared
interface PatternCompiler {
    fun compile(spec: PatternSpec, hardwareVersion: Int, firmwareVersion: String): DeviceCommandPlan
    // Трансляция сложных пространственных анимаций (Chase, Wave) в последовательности команд для 8-диодного кольца
    // Оркестрация последовательного выполнения команд для поддержки SEQUENTIAL режима ("секретные коды") путем генерации команд с задержками
    // Специализированная обработка PatternElementSequence для эффективной компиляции дискретных последовательностей
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
  - Учитывать `hardwareVersion` (100 — кольцо 8 LED, 200 — кольцо 8 LED): деградация/апгрейд эффектов.
  - Учитывать `firmwareVersion` (например, `2.1.0`): поддержка новых команд (например, `Sparkle`), обратная совместимость, миграция устаревших команд.
  - Нормализация параметров (скорости, интенсивности) в допустимые диапазоны прошивки.
  - Валидация и фолбэки для неподдерживаемых `PatternElement`.
  - **Специальная обработка `PatternElementSequence`**: Прямая трансляция `SequenceStep` в BLE-команды `SET_LED` и `DELAY` без промежуточных преобразований, обеспечивая максимальную эффективность для "секретных кодов".
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

Статусы загрузки для UX:

- `:core:ble` предоставляет реактивный поток прогресса загрузки анимации `Flow<UploadProgress>` для чёткой обратной связи в UI.
- Контракт прогресса (упрощённо):
```kotlin
// :core:ble
data class UploadProgress(
    val totalChunks: Int,
    val sentChunks: Int,
    val percent: Int,
    val state: UploadState
)

sealed interface UploadState {
    data object Preparing : UploadState
    data object Uploading : UploadState
    data object Committing : UploadState
    data object Completed : UploadState
    data class Failed(val cause: Throwable?) : UploadState
}

interface BleCommandEncoder {
    fun encode(plan: DeviceCommandPlan, mtu: Int): List<ByteArray>
    fun upload(plan: DeviceCommandPlan, mtu: Int): Flow<UploadProgress>
}
```

- Поведение:
  - В случае ошибки на N‑м чанке поток эмитит `Failed(cause)`; ViewModel показывает ошибку и кнопку «Повторить» (ре‑вызов `upload`).
  - При успешном завершении — `Completed`, UI переводится в «Готово» и может предложить предпросмотр.
  - Поток «горячий» на время операции; отмена корутины пользователем прерывает передачу и отправляет `ROLLBACK`.

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

### Схема взаимодействия и контрактов

```
┌─────────────────── PRESENTATION (UI Layer) ────────────────────┐
│   Composable Screen  <-- (StateFlow<State>, Flow<Effect>) ---  ViewModel   │
│         │                                                          ▲        │
│         └-------------------- (Event) ----------------------------┘        │
└───────────────────────────────────▲────────────────────────────────────────┘
                                    │
                                (UseCase)
                                    │
┌─────────────────── DOMAIN (:shared) ───────────────────────────┐
│                                                                        │
│   UseCase  <-- (Result<DomainModel>, Flow<DomainModel>) --- Repository (Interface) │
│                                                                        │
└───────────────────────────────────▲────────────────────────────────────────┘
                                    │
                         (Repository Implementation)
                                    │
┌──────────────────── DATA & CORE (Android Layer) ────────────────┐
│   Repository (Impl)  -->  ApiService, DAO, AmuletBleManager...       │
└──────────────────────────────────────────────────────────────────┘
```

Эта схема визуализирует, какие типы данных передаются между слоями, что и является сутью контрактов.

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


### Движок правил (IFTTT / Control Center) — стратегия исполнения

Задача: обеспечить надёжное и кросс‑платформенное выполнение пользовательских правил «ЕСЛИ → ТО».

Гибридная модель исполнения:

- Клиентские триггеры (on‑device, мгновенные):
  - Жесты устройства (одиночное/двойное касание, удержание), локальные события приложения (вход в экран, локальные таймеры во время активной сессии).
  - Исполняются на клиенте для минимальной задержки, особенно когда требуется немедленная реакция на подключённый Амулет (BLE).
  - Оркестрация через `:shared` UseCase/оркестраторы, отправка команд через `:core:ble`.
- Серверные триггеры (внешние, расписания, интеграции):
  - Погода, календарь, вебхуки, ночные/ежедневные окна — исполняются на бэкенде.
  - Периодический тикер (например, `/v1/rules.tick`) агрегирует и проверяет условия; при срабатывании выполняет действие (FCM data‑push к клиенту/команда в очередь/изменение состояния).
  - Преимущества: надёжность, независимость от состояния приложения/платформы, энергоэффективность.

Роль `:data:rules`:

- Инкапсулирует API и локальное состояние правил; предоставляет единый интерфейс для CRUD и выполнения локальных правил.
- Делегирует проверку триггеров согласно типу:
  - `LocalTrigger` → локальная обработка (например, жесты/внутренние события) с немедленной реакцией.
  - `RemoteTrigger`/`ScheduledTrigger` → регистрация/синхронизация с бэкендом; исполнение на сервере, доставка через FCM/поллинг‑бекоф.
- Поддерживает офлайн‑редактирование правил с последующей синхронизацией и идемпотентными обновлениями.

Контракт (упрощённо):

```kotlin
// :shared
sealed interface RuleTriggerKind { object Local : RuleTriggerKind; object Remote : RuleTriggerKind }

interface RulesRepository {
    fun observeRules(): Flow<List<Rule>>
    suspend fun createOrUpdate(rule: RuleDraft): Rule
    suspend fun delete(ruleId: String)
    suspend fun evaluateLocalTriggers(event: LocalEvent) // для on‑device событий
}

// :data:rules — реализация
class RulesRepositoryImpl(...): RulesRepository {
    override suspend fun evaluateLocalTriggers(event: LocalEvent) {
        // фильтрация правил с LocalTrigger и выполнение действий (через UseCase/BLE)
    }
}
```

Политики и надёжность:

- На клиенте: выполнение только «дешёвых» и контекстно‑локальных правил в пределах активной сессии/ForegroundService; без постоянных фоновых воркеров.
- На сервере: SLA исполнения по расписаниям и внешним условиям; доставка эффектов через FCM data‑messages (см. Real‑Time раздел).
- Конфликты и приоритеты: если одно правило блокирует другое, разрешение определяется политикой (приоритет/последовательность); логирование телеметрии в `:core:telemetry` с учётом согласий.

### Оркестраторы бизнес‑процессов (Coordinators/Orchestrators)

Цель: снизить связанность UseCase’ов, повысить транзакционность и выразительность сложных сценариев. Оркестраторы живут в `:shared` (KMP), чтобы их могли использовать ViewModel на всех платформах.

- Когда применять (хорошие кандидаты):
  - Регистрация + Онбординг (много шагов: профиль → согласия → уведомления → обучение).
  - Привязка устройства (NFC → облачная привязка → BLE‑настройка → тест).
  - Создание сложного правила в «Control Center» (пошаговый мастер с валидациями и превью).
  - Принятие приглашения в Пару (валидации, конфликтные состояния, пост‑действия).
- Когда НЕ применять (простые операции):
  - Отправка «объятия», получение списка практик, изменение имени пользователя — прямой вызов UseCase из ViewModel.

Принципы:

- Оркестратор инкапсулирует последовательность UseCase’ов, включая валидации, компенсации, ретраи и согласованность состояния.
- Возвращает расширенный результат процесса (успех/ошибки по шагам, частичные исходы, рекомендации для UI).
- Имеет чёткую модель состояния и событий (мини‑стейт‑машина внутри Domain).
- Независим от платформы и инфраструктуры (никаких Android‑API внутри).

Транзакционность и компенсации:

- Стратегии отката: если шаг N+1 упал, выполнять компенсирующее действие для шага N (например, отвязать устройство в облаке при неудачном BLE‑прописывании).
- Идемпотентность шагов: повторный запуск безопасен (проверки «уже выполнено»).
- Ретраи с бэкофом, лимиты попыток, сохранение прогресса в офлайне при необходимости (совместно с outbox‑очередью).

Интеграция с ViewModel:

- ViewModel подписывается на `state` оркестратора, маппит в `ScreenState`/`SideEffect`.
- UI вызывает `start/retry/cancel`; простые операции по‑прежнему идут напрямую в UseCase.
- DI: оркестраторы предоставляются через DI из `:shared`; зависимости (UseCase/Repo) внедряются в них так же, как и в UseCase’ы.


### 5. Кросс‑платформенная стратегия (Kotlin Multiplatform Strategy)

Роль `:shared`:

- Домейн‑слой: интерфейсы репозиториев, UseCase, бизнес‑правила, модели/DTO (kotlinx.serialization) и мапперы.
- Портируемость: iOS/other платформы смогут переиспользовать домейн без Android‑зависимостей.

Что остаётся на платформе (Android):

- Реализации репозиториев (сеть `Retrofit`, БД `Room`, BLE/NFC), все адаптеры инфраструктуры, UI (Compose), DI‑wireup (Hilt), уведомления/сервисы.

Связывание:

- Android внедряет реализации для интерфейсов из `:shared` через DI. Для KMP рекомендуется Koin в `:shared` с абстрактными модулями и Hilt‑мостом на Android.

DI в `:shared` (KMP‑совместимо):

- Рекомендация: использовать Koin (или Kodein) внутри `:shared` для связывания UseCase’ов и доменных интерфейсов. Это позволяет переиспользовать контейнер на iOS и в Unit‑тестах без Android‑зависимостей.


Сосуществование с Hilt:

- Биндинг репозиториев делается Hilt‑модулем, который делегирует в Koin.

Мост Hilt ↔ Koin (в `:app`):

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object KoinBridgeModule {

    @Provides
    @Singleton
    fun provideKoin(app: Application): Koin {
        val koinApp = startKoin {
            androidContext(app)
            modules(
                sharedKoinModules() +
                listOf(androidDataModules)
            )
        }
        return koinApp.koin
    }

    @Provides
    fun provideSendHugUseCase(koin: Koin): SendHugUseCase = koin.get()

    @Provides
    fun provideHugsRepository(koin: Koin): HugsRepository = koin.get()
}
```

Пояснения:

- Единый источник биндингов UseCase/Repo — Koin в `:shared` (+ платформенные модули). Hilt «поднимает» эти зависимости в свой граф через мост.
- ViewModel под Hilt получает зависимости через обычные `@Inject`/`@HiltViewModel`, инстансы берутся из Koin.
- Подход сохраняет строгую изоляцию фич и упрощает тестирование (можно запускать Koin‑модули отдельно).

iOS:

- `startKoin { modules(sharedKoinModules() + iosModules) }` вызывается на старте приложения, где `iosModules` предоставляет реализации репозиториев под iOS.


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
- Feature Toggles и удалённая конфигурация: `:core:config` оборачивает Firebase Remote Config; значения по умолчанию зашиты в приложение; UseCase’ы зависят от интерфейса `ConfigRepository`.


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

Требования к `:core:telemetry` и согласиям (privacy/consents):

- Любая отправка события телеметрии должна предваряться проверкой согласий пользователя (см. `User.consents` в API и локальной модели).
- Гейтинг событий по категориям согласий (например, `analytics`, `usage`, `crash`, `diagnostics`). Если согласие отсутствует — событие отбрасывается до попадания в очередь.
- Локальный кэш согласий с «быстрым» доступом (DataStore/в памяти) и реактивными обновлениями. Изменение согласий немедленно отражается на пайплайне событий.
- Конфигурация:
  - Белый список типов событий и требуемые для них согласия.
  - Политики хранения: TTL, максимальный объём локальной очереди, поведение при офлайне (drop/keep‑latest).
- Минимизация данных: исключать PII, хешировать/анонимизировать идентификаторы там, где возможно; обеспечить режим «no‑ID» при отсутствии согласий.
- Прозрачность и отзыв: API для полного отключения телеметрии и очистки локальной очереди при отзыве согласий.
- Аудит: счётчики принятых/отклонённых событий по причинам (нет согласия, схема неразрешена), экспорт агрегатов для экрана приватности.


### Руководство по внесению изменений

- Любое изменение API/схем — синхронизировать модели `:shared` и конвертеры `:core:network`.
- Новая фича — отдельный `:feature:*` модуль, публичные контракты в `:shared`.
- Диаграмма зависимостей проверяется static‑линтером (Forbidden APIs/deps rules).
- Добавляйте тесты: unit для UseCase/редьюсеров, интеграционные для источников данных, UI для основных сценариев.

### Стратегия навигации и передачи данных

Проблема: рост графа навигации увеличивает сложность и риск ошибок при передаче аргументов. Решение — типобезопасные маршруты, строгие правила передачи данных и scoped‑состояние.

- Типобезопасная навигация:
  - Используйте helper‑функции/генераторы маршрутов: `navigateToDetails(itemId: String)` вместо строковых путей `navigate("details/123")`.
  - Статически описывайте аргументы и deep link паттерны для каждого destination.
  - Возврат результатов — через `SavedStateHandle`/`BackStackEntry` contract (официальный паттерн Compose).
- Передача данных:
  - Простые ID: передавайте только идентификаторы (`practiceId`, `patternId`); экран сам загружает данные, соблюдая Single Source of Truth.
  - Сложные объекты: не передавать Parcelable/Serializable через аргументы — ограничение по размеру/хрупкость.
  - Альтернатива: `SharedViewModel`, живущая в scope навигационного графа. Один экран записывает состояние, другой считывает.
- Deep Links:
  - Публичные экраны (профиль партнёра, конкретная практика/паттерн) имеют собственные deep link’и; поддержка NFC‑entry и уведомлений.
  - Гейтинг через `:core:config`/фичефлаги и проверку авторизации перед навигацией.
  
### Удалённая конфигурация и Feature Toggles

Задача: включать/выключать фичи без релиза, проводить A/B тесты, иметь «kill‑switches» на случай инцидентов.

- Модуль: `:core:config`.
- Контракты:
```kotlin
interface ConfigRepository {
    fun observe(): Flow<AppConfig>
    suspend fun getBoolean(key: String, default: Boolean = false): Boolean
    suspend fun getString(key: String, default: String = ""): String
    suspend fun refresh(force: Boolean = false)
}

data class AppConfig(
    val flags: Map<String, Boolean>,
    val strings: Map<String, String>,
    val lastFetchedAt: Long
)
```

- Реализация: Firebase Remote Config + локальный кэш (DataStore). Значения по умолчанию жёстко зашиваются в приложении (res/raw или hardcoded в `DefaultsProvider`).
- Поведение при первом запуске/офлайне: используются дефолты; фетч откладывается до появления сети.
- Интеграция с UseCase: специализированные UseCase’ы читают флаги, например `IsCrystalShellsFeatureEnabledUseCase`.
- A/B тесты: ключи флагов и вариантов определяются на стороне Remote Config; клиент получает ветку как обычное значение; аналитика учитывает вариацию.
- Kill‑switches: критичные флаги (например, `hugs_sending_enabled`) читаются синхронно/раньше и используются в проверках до выполнения действий.


### Глоссарий

- ScreenState — неизменяемая модель состояния экрана, источник истины для UI.
- SideEffect — одноразовое событие (навигация, тост), не часть сохранённого состояния.
- UseCase — атомарная бизнес‑операция (напр., SendHugUseCase).
- Repository — фасад к данным (Remote/Local/BLE), инкапсулирующий источники и политики консистентности.


