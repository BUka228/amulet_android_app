### Суть документа

Этот документ определяет централизованную стратегию управления состоянием аутентификации и контекстом текущего пользователя. Его цель — создать единый, быстрый, типобезопасный и реактивный источник правды о сессии, который будет использоваться всеми слоями приложения.

**Проблема, которую мы решаем:**
- Избежать дублирования логики проверки статуса "залогинен/не залогинен".
- Предотвратить множественные запросы к БД/сети за одними и теми же базовыми данными пользователя (ID, согласия).
- Обеспечить атомарное обновление состояния сессии, исключая гонку состояний.
- Создать чистый и масштабируемый механизм для реакции всего приложения на вход или выход пользователя.

**Решение:**
Мы вводим `UserSessionManager` — компонент в модуле `:core:auth`, который предоставляет единый поток `StateFlow<UserSessionContext>`. Этот поток является единственным источником правды о состоянии сессии. Управление этим состоянием (логин, логаут) делегируется `AuthRepository`, который, в свою очередь, координируется `UseCase`'ами из доменного слоя.

---

### 1. Ключевые компоненты и их роли

| Компонент | Модуль | Роль |
| :--- | :--- | :--- |
| **`UserSessionManager`** | `:core:auth` | **Пассивный хранитель состояния.** Хранит и предоставляет `UserSessionContext` для всего приложения. Не содержит бизнес-логики. |
| **`UserSessionContext`** | `:core:auth` | **Иммутабельная модель данных.** Полностью описывает текущее состояние сессии (`Loading`, `LoggedOut`, `LoggedIn`). |
| **`AuthRepository`** | `:data:auth` | **Менеджер процесса аутентификации.** Отвечает за взаимодействие с Firebase/API для входа/выхода и **управляет** состоянием в `UserSessionManager`. |
| **`SignInUseCase`** (Пример) | `:shared` | **Оркестратор бизнес-процесса.** Координирует вызовы `AuthRepository` и `UserRepository` для выполнения полного флоу входа пользователя. |

---

### 2. Диаграмма потока данных (Пример: Вход пользователя)

Эта диаграмма показывает правильное взаимодействие между слоями без нарушения зависимостей. `AuthRepository` и `UserRepository` не общаются напрямую.

```mermaid
sequenceDiagram
    participant ViewModel
    participant SignInUseCase
    participant AuthRepository
    participant UserRepository
    participant UserSessionManager
    participant Firebase/API

    ViewModel->>+SignInUseCase: invoke(email, password)
    Note over SignInUseCase: Оркестрация начинается в Domain слое

    SignInUseCase->>+AuthRepository: signIn(email, password)
    AuthRepository->>+Firebase/API: authenticate()
    Firebase/API-->>-AuthRepository: Success (uid)
    AuthRepository-->>-SignInUseCase: Result.success(uid)

    SignInUseCase->>+UserRepository: getUserProfile(uid)
    UserRepository->>+Firebase/API: GET /users.me
    Firebase/API-->>-UserRepository: User Profile Data
    UserRepository-->>-SignInUseCase: Result.success(User object)

    Note over SignInUseCase: Данные собраны. Устанавливаем сессию.

    SignInUseCase->>+AuthRepository: establishSession(user)
    AuthRepository->>+UserSessionManager: updateSession(user)
    UserSessionManager-->>-AuthRepository: OK

    AuthRepository-->>-SignInUseCase: OK
    SignInUseCase-->>-ViewModel: Result.success()
```

---

### 3. Контракты и канонический код

#### UserSessionContext.kt (`:core:auth`)

```kotlin
/**
 * Sealed interface, описывающий все возможные состояния сессии.
 * Является единым источником правды для всего приложения.
 */
sealed interface UserSessionContext {
    /**
     * Начальное состояние при запуске приложения, до того как сессия была проверена.
     * UI на этом состоянии должен показывать сплэш-скрин.
     */
    object Loading : UserSessionContext

    /**
     * Терминальное состояние, когда пользователь не аутентифицирован.
     * UI на этом состоянии должен показывать экраны входа/регистрации.
     */
    object LoggedOut : UserSessionContext

    /**
     * Состояние активной сессии. Содержит легковесный, критически важный
     * и часто используемый контекст пользователя.
     *
     * ВАЖНО: Не храните здесь полную, "тяжелую" модель User из доменного слоя.
     * Этот объект предназначен для быстрого доступа. За полной моделью User
     * следует обращаться в UserRepository.
     */
    data class LoggedIn(
        val userId: String,
        val displayName: String?,
        val avatarUrl: String?,
        val consents: UserConsents // Модель согласий из :shared
    ) : UserSessionContext
}
```

#### UserSessionManager.kt (`:core:auth`)

```kotlin
/**
 * Основной интерфейс для доступа к контексту сессии.
 * Реализация этого интерфейса является синглтоном на уровне приложения.
 */
interface UserSessionManager {

    /**
     * Основной реактивный поток состояния сессии.
     * Подпишитесь на него, чтобы реагировать на вход/выход пользователя.
     */
    val sessionContext: StateFlow<UserSessionContext>

    /**
     * Удобное свойство для синхронного доступа к текущему состоянию.
     * Идеально для мест, где не нужна реактивность (например, Interceptors).
     */
    val currentContext: UserSessionContext get() = sessionContext.value

    /**
     * Обновляет сессию, переводя ее в состояние LoggedIn.
     * ВНИМАНИЕ: Этот метод должен вызываться ИСКЛЮЧИТЕЛЬНО из AuthRepository.
     */
    suspend fun updateSession(user: User)

    /**
     * Очищает сессию, переводя ее в состояние LoggedOut.
     * ВНИМАНИЕ: Этот метод должен вызываться ИСКЛЮЧИТЕЛЬНО из AuthRepository.
     */
    suspend fun clearSession()
}
```

#### AuthRepository.kt (интерфейс в `:shared`)

```kotlin
/**
 * Репозиторий, отвечающий за процессы аутентификации и управление сессией.
 */
interface AuthRepository {
    /**
     * Выполняет аутентификацию пользователя.
     * При успехе возвращает уникальный идентификатор пользователя (uid).
     */
    suspend fun signIn(credentials: UserCredentials): Result<String, AppError>

    /**
     * Завершает сессию пользователя.
     */
    suspend fun signOut(): Result<Unit, AppError>

    /**
     * Устанавливает глобальный контекст сессии после полной оркестрации.
     * Принимает полную модель User и передает ее в UserSessionManager.
     */
    suspend fun establishSession(user: User): Result<Unit, AppError>
}
```

---

### 4. Интеграция с Proto DataStore

Для персистентного хранения сессии и быстрого восстановления при перезапуске приложения мы используем **Proto DataStore**.

- **Почему Proto?** Он обеспечивает типобезопасность, явную схему, высокую производительность (бинарный формат) и безопасную миграцию, что полностью соответствует нашим требованиям к эталонному приложению.
- **Размещение:** Файл `.proto` и реализация `DataStore` находятся в модуле `:core:auth`. `UserSessionManagerImpl` будет использовать `DataStore` как деталь своей реализации, скрывая ее от остального приложения.

**`user_session.proto` (в `:core:auth`):**
```protobuf
syntax = "proto3";

option java_package = "com.example.amulet.core.auth.datastore";
option java_multiple_files = true;

// Хранилище для легковесного контекста сессии.
message UserSessionPreferences {
  string user_id = 1;
  string display_name = 2;
  string avatar_url = 3;
  UserConsentsProto consents = 4;
}

message UserConsentsProto {
  bool analytics = 1;
  // ... другие поля согласий
}
```

#### 4.1. Безопасность и хранение

- **Инструмент хранения:** Для персистентного хранения `UserSessionContext` используется Proto DataStore.
- **Безопасность:** Хранение данных зашифровано. Даже при прямом доступе к файлу на рутованном устройстве утечки PII не произойдет.
- **Механизм шифрования:**
  - Используется официальная библиотека `androidx.security:security-crypto`.
  - DataStore настроен на работу через `EncryptedFile`, обеспечивающий шифрование по схеме AES‑256‑GCM.
  - Ключи шифрования управляются системой Android через Android Keystore (`MasterKey`), что является золотым стандартом безопасности на платформе.
- **Связь с общей стратегией:** Подход соответствует общей стратегии безопасности, описанной в документе `docs/50_CROSS_CUTTING_CONCERNS/03_SECURITY_AND_PRIVACY.md`.

---

### 5. Чек-лист для разработчика

- ✅ Для получения реактивного состояния сессии — подписывайтесь на `userSessionManager.sessionContext`.
- ✅ Для синхронного доступа к `userId` или `consents` (в интерсепторах, логгерах) — используйте `userSessionManager.currentContext`.
- ❌ **ЗАПРЕЩЕНО** вызывать `updateSession` или `clearSession` откуда-либо, кроме `AuthRepositoryImpl`.
- ✅ Процесс входа/регистрации должен быть оркестрирован через `UseCase` в доменном слое.
- ❌ **ЗАПРЕЩЕНО** создавать зависимости между репозиториями (например, `AuthRepository` -> `UserRepository`).


