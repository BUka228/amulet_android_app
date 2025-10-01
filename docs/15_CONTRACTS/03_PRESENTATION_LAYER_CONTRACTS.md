## Контракты Presentation Layer — ViewModel

ViewModel управляет состоянием одного экрана/фичи, обрабатывает действия пользователя и связывает UI с Domain. Следует MVVM+ (с практиками MVI). Контракт тесно связан с `40_PRESENTATION_LAYER/01_UI_STATE_MANAGEMENT.md`.

---

### 1. Назначение и именование

- Назначение: единственный источник истины для состояния экрана, редьюсер доменных результатов → `ScreenState`.
- Именование: `[ScreenName]ViewModel` (например, `ProfileViewModel`, `HugsViewModel`).

### 2. Зависимости

- Разрешено: только UseCase (иногда оркестраторы из `:shared`).
- Запрещено: прямые зависимости от репозиториев, DAO, ApiService, Android UI API.

DI: Hilt/Koin‑мост. Жизненный цикл корутин — `viewModelScope`.

### 3. Контракт с UI

- Вход от UI: единый метод
  - `fun handleEvent(event: UiEvent)` — события экрана (sealed interface).
- Выход в UI:
  - `val uiState: StateFlow<ScreenState>` — декларативное состояние.
  - `val sideEffect: SharedFlow<SideEffect>` — одноразовые события (навигация, snackbar, диалоги).

Правила:
- `ScreenState` — иммутабельный `data class`, обновления через `.copy()`.
- Side effects — отдельный `SharedFlow` с `replay = 0`.
- Ошибки — поле `error: AppError?` в состоянии и/или side effects для кратких уведомлений.

### 4. Внутренняя логика

- ViewModel вызывает UseCase в `viewModelScope.launch { ... }` или подписывается на `Flow` UseCase.
- Результаты UseCase (`Result`/`Flow`) преобразуются в новый `ScreenState` (редьюсинг).
- Управление жизненным циклом: отмена корутин при очистке VM, интеграция с SavedStateHandle при необходимости.

### 5. Канонический пример

```kotlin
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val observeUserProfileUseCase: ObserveUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileState(isLoading = true))
    val uiState: StateFlow<ProfileState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<ProfileSideEffect>()
    val sideEffect: SharedFlow<ProfileSideEffect> = _sideEffect.asSharedFlow()

    init { observeProfile() }

    fun handleEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.SaveChanges -> save(event.draft)
            is ProfileEvent.Retry -> observeProfile()
            // ... другие события
        }
    }

    private fun observeProfile() {
        observeUserProfileUseCase()
            .onEach { user ->
                _uiState.update { it.copy(isLoading = false, user = user) }
            }
            .launchIn(viewModelScope)
    }

    private fun save(draft: UserProfileDraft) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            updateUserProfileUseCase(draft)
                .onSuccess { _sideEffect.emit(ProfileSideEffect.ShowSnackbar("Сохранено!")) }
                .onFailure { error -> _uiState.update { it.copy(error = error) } }
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}
```

### 6. Интеграция с Paging 3

- Для списков: хранить `LazyPagingItems` вне `ScreenState` (как поток/отдельное поле), а маппинг `LoadState` отражать в `ScreenState`.
- Обновлять `refresh/append/prepend` состояния, аккуратно маппить ошибки `LoadState.Error` → `AppError`.

### 7. SavedStateHandle и process‑death

- Для критичных незавершённых данных (редакторов) — сохранять в `SavedStateHandle` сериализованные значения.
- Восстанавливать состояние в `init {}` и предоставлять события для очистки/сохранения.

### 8. Чек‑лист для новых ViewModel

- Один метод входа `handleEvent(event)`; все UI‑действия моделируются как `UiEvent`
- `uiState: StateFlow<ScreenState>`, `sideEffect: SharedFlow<SideEffect>`
- Внутри — только UseCase/оркестраторы; никакой инфраструктуры Data/Core
- Ошибки типизированы `AppError`, отображение согласовано с `Error Handling`
- Коррутины в `viewModelScope`, отмена и обработка `CancellationException` корректны
- Paging/SavedStateHandle подключены по необходимости

---

См. подробно UI‑контракты и примеры в `docs/40_PRESENTATION_LAYER/01_UI_STATE_MANAGEMENT.md`.

