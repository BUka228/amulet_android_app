## Контракты Domain Layer — UseCase (Interactors)

UseCase инкапсулирует одну конкретную бизнес‑операцию (глагол). Не содержит состояния, легко тестируется, зависит только от интерфейсов репозиториев из `:shared`.

Основано на: правилах модульности (`10_ARCHITECTURE/02_MODULARIZATION.md`), потоках данных и ошибках (`01_ARCHITECTURE_OVERVIEW.md`, `50_CROSS_CUTTING_CONCERNS/01_ERROR_HANDLING.md`).

---

### 1. Назначение и принципы

- UseCase выражает атомарное действие/запрос домена. Примеры: `SendHugUseCase`, `ObserveUserProfileUseCase`.
- Всегда один публичный метод — как правило, `operator fun invoke(...)` или `execute(...)`.
- Не знает о платформах/фреймворках (Android/Retrofit/Room/Compose/Hilt) — только KMP‑совместимый код.
- Валидация входных данных и лёгкая бизнес‑логика допускаются внутри UseCase.

### 2. Именование

- Шаблон: `[Verb][Noun]UseCase`.
- Командные операции (одноразовые): `SendHugUseCase`, `UpdateUserProfileUseCase`, `PairAcceptUseCase`.
- Запросы/наблюдения (потоки): `ObserveUserProfileUseCase`, `GetHugsUseCase` (если возвращает поток пагинации).

### 3. Зависимости

- Разрешено: только интерфейсы `*Repository` из `:shared`.
- Запрещено: зависимости на реализации репозиториев, DAO, ApiService, ViewModel, UI, Android SDK.
- DI: предоставляются через Koin/Hilt‑мост (см. `01_ARCHITECTURE_OVERVIEW.md`).

### 4. Контракт методов

- Входные параметры: только необходимые для операции данные (примитивы/доменные модели).
- Возвращаемые значения:
  - Команды: `suspend operator fun invoke(...): Result<T, AppError>`.
  - Потоки: `operator fun invoke(...): Flow<T>` или `Flow<Result<T, AppError>>` — согласно контракту соответствующего репозитория.
  - Paging: `operator fun invoke(...): Flow<PagingData<T>>`.

Все ошибки типизированы `AppError`. Для цепочек нескольких операций используйте `Result`‑комбинаторы и/или оркестраторы (для сложных процессов).

### 5. Канонические примеры

```kotlin
// Команда (одноразовое действие)
class SendHugUseCase @Inject constructor(
    private val hugsRepository: HugsRepository
) {
    suspend operator fun invoke(draft: HugSendDraft): Result<Hug, AppError> {
        // Доп. валидация/бизнес‑правила — при необходимости
        return hugsRepository.sendHug(draft)
    }
}
```

```kotlin
// Запрос (поток данных)
class ObserveUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<User> {
        // Комбинирование/фильтрация допускаются здесь
        return userRepository.observeCurrentUser()
    }
}
```

```kotlin
// Пагинация
class GetHugsUseCase @Inject constructor(
    private val hugsRepository: HugsRepository
) {
    operator fun invoke(direction: HugDirection): Flow<PagingData<Hug>> =
        hugsRepository.getHugsStream(direction)
}
```

### 6. Политики ошибок и ретраев

- UseCase НЕ преобразует `AppError` в исключения; ошибки остаются типизированными.
- Допускается добавлять политику повторов для временных ошибок (см. `retryWithBackoff` в `:shared`).
- Специфичные ответы (например, `RateLimited`, `VersionConflict`) не скрываются — они важны для UI.

### 7. Тестирование

- Юнит‑тесты UseCase должны мокировать только интерфейсы репозиториев.
- Проверять ветвления по ошибкам/успеху, простую валидацию.

### 8. Чек‑лист для новых UseCase

- Имя в форме «Глагол+Сущность+UseCase», один публичный метод `invoke/execute`
- Зависит только от интерфейсов репозиториев
- Принимает минимально достаточные аргументы, возвращает `Result`/`Flow`
- Не содержит состояния; чистая бизнес‑логика, без Android/IO API
- Ошибки типизированы `AppError`, без `Throwable` наружу

---

Примечание: Сложные сценарии с несколькими шагами оформляйте как «Оркестраторы» в `:shared` (см. `01_ARCHITECTURE_OVERVIEW.md`, раздел «Оркестраторы бизнес‑процессов»).


