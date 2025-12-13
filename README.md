# Amulet Android App

Android‑приложение для экосистемы **Amulet**. Приложение выступает центром управления физическим устройством Amulet (BLE/NFC, OTA), а также предоставляет практики для ментального здоровья, социальные функции («объятия»), библиотеку и редактор паттернов.

## Полезные ссылки по проекту

- `docs/00_PRODUCT/02_Концепт_документ.md`
- `docs/00_PRODUCT/01_Сценарии_использования.md`
- `docs/10_ARCHITECTURE/01_ARCHITECTURE_OVERVIEW.md`
- `docs/10_ARCHITECTURE/02_MODULARIZATION.md`
- `docs/20_DATA_LAYER/03_BLE_PROTOCOL.md`

## Технологический стек и архитектура

- **Язык:** Kotlin
- **UI:** Jetpack Compose + Navigation Compose
- **Архитектура:** Clean Architecture + UDF; в презентации MVVM с практиками MVI
- **Асинхронность:** Coroutines + Flow
- **DI:** Hilt (Android) + Koin (KMP `:shared`, подключение через bridge в `:app`)
- **Сеть:** Retrofit + OkHttp + kotlinx.serialization
- **Локальное хранение:** Room + DataStore
- **Offline-first синхронизация:** Outbox + WorkManager (`:core:sync`)
- **BLE:** собственный протокол поверх GATT (`:core:ble`, см. `docs/20_DATA_LAYER/03_BLE_PROTOCOL.md`)

Версии задаются через Gradle Version Catalog: `gradle/libs.versions.toml`.

## Сборка и запуск

### Требования

- Android Studio (актуальная версия)
- JDK 17

### Настройка `local.properties`

Приложение читает параметры из `local.properties` и прокидывает их в `BuildConfig` (см. `app/build.gradle.kts`).

Создай/обнови `local.properties` в корне проекта и добавь ключи **без кавычек**:

```properties
# Supabase
SUPABASE_URL=https://<your-project>.supabase.co
SUPABASE_REST_URL=https://api.amulet.app/v2
SUPABASE_ANON_KEY=<your_anon_key>

# Turnstile
TURNSTILE_SITE_KEY=<your_site_key>

# OneSignal
ONESIGNAL_APP_ID=<your_app_id>
```

Важно: не коммить секреты и не добавляй их в README.

### Запуск

- Открой проект в Android Studio.
- Дождись завершения Gradle Sync.
- Запусти конфигурацию `app` на эмуляторе/устройстве.

## Модульная структура

Проект многомодульный (см. `settings.gradle.kts`). Крупные группы:

- **`:app`** — Android‑приложение: Compose entrypoint, навигация, DI‑композиция.
- **`:shared`** — KMP‑модуль с доменными моделями/контрактами/UseCase’ами.
- **`:core:*`** — инфраструктура:
  - `:core:network` (Retrofit/OkHttp, интерсепторы, маппинг ошибок)
  - `:core:database` (Room, DAO)
  - `:core:sync` (Outbox + WorkManager)
  - `:core:ble` (BLE менеджер, flow‑control, OTA)
  - `:core:design` (дизайн‑система Compose)
  - и др. (`auth`, `supabase`, `telemetry`, `notifications`, `foreground`, ...)
- **`:data:*`** — реализации репозиториев/источников данных (network + DB + outbox).
- **`:feature:*`** — пользовательские сценарии (auth/dashboard/devices/patterns/practices/hugs/onboarding/settings и т.д.).

## Примечания по реализации

- **Outbox/WorkManager** обеспечивает гарантированную доставку действий при сетевых ошибках (пример: отправка «объятий» в `:data:hugs`).
- **BLE слой** отдаёт состояние и данные через `Flow`/`StateFlow` и реализует OTA/загрузку анимаций с flow‑control.


