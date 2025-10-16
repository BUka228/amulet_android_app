# Core: Supabase

Модуль инкапсулирует всю работу с Supabase SDK, включая автоматическое управление токенами аутентификации.

## Архитектура

### Компоненты

#### SupabaseSessionStorage
- **Назначение**: Персистентное хранилище Supabase-сессий через DataStore Preferences
- **Функции**:
  - `saveSession()` — сохранение сессии
  - `loadSession()` — восстановление сессии
  - `clearSession()` — очистка сессии
  - `observeSession()` — реактивное наблюдение за изменениями

#### SupabaseAuthSessionManager
- **Назначение**: Кастомная реализация `SessionManager` для Supabase SDK
- **Интеграция**: Подключается в `install(Auth)` конфигурации SupabaseClient
- **Поведение**: Автоматически сохраняет/восстанавливает сессии через SupabaseSessionStorage

#### SupabaseAuthManager
- **Назначение**: Централизованное управление аутентификацией и токенами
- **Функции**:
  - `getAccessToken()` — получение access token с автоматическим обновлением
  - `getAuthorizationHeader()` — формирование полного Authorization заголовка (Bearer xxx)
  - `refreshToken()` — принудительное обновление токена
  - `clearSession()` — очистка сессии (logout)
- **Механизм обновления**: Автоматически обновляет токены за 60 секунд до истечения

#### SupabaseIdTokenProvider
- **Назначение**: Реализация `IdTokenProvider` для AuthInterceptor
- **Binding**: Предоставляется через DI как singleton
- **Использование**: Автоматически добавляет Authorization заголовок ко всем HTTP-запросам

## Конфигурация

### SupabaseModule

```kotlin
install(Auth) {
    autoLoadFromStorage = true      // Автозагрузка при старте
    autoSaveToStorage = true         // Автосохранение после auth операций
    alwaysAutoRefresh = true         // Автообновление токенов
    sessionManager = SupabaseAuthSessionManager  // Кастомный менеджер
}
```

### DI биндинги
- `IdTokenProvider` → `SupabaseIdTokenProvider` (для AuthInterceptor)
- `SupabaseClient` → настроенный клиент с Auth плагином
- `DataStore<Preferences>` → `supabase_session` хранилище

## Преимущества

1. **Автоматизация**: Токены автоматически сохраняются, восстанавливаются и обновляются
2. **Инкапсуляция**: Детали работы с токенами скрыты от других модулей
3. **Персистентность**: Сессии сохраняются между перезапусками приложения
4. **Безопасность**: Токены не передаются через shared/domain слои
5. **Централизация**: Вся логика токенов в одном месте

## Интеграция с другими модулями

### core:network
- Получает `IdTokenProvider` через DI
- `AuthInterceptor` автоматически добавляет заголовки к запросам

### data:auth
- Использует `SupabaseClient` для auth операций
- Не управляет токенами напрямую (это делает Supabase SDK)

### shared
- Не знает о токенах
- `UserSessionContext` содержит только пользовательские данные

### core:auth
- Управляет только пользовательской сессией (displayName, consents и т.д.)
- Не хранит и не управляет токенами
