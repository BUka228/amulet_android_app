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

Диаграмма зависимостей (упрощённо):

```
:app
 ├─ :feature:* ──┐
 │               ├─ :shared
 │               ├─ :core:design
 │               └─ :data:* ──┐
 │                           ├─ :shared (interfaces, DTO)
 │                           ├─ :core:network
 │                           ├─ :core:database
 │                           └─ :core:ble (if needed)
 └─ DI wires implementations (Hilt)
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
- Конструктор анимаций: модели паттернов совместимы со схемами `/components/schemas/Pattern*`; адаптация по `hardwareVersion` (100/200). Конвертер в низкоуровневые BLE‑команды инкапсулирован в `:core:ble`.
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


