# UI State Management - Контракт между ViewModel и UI

Данный документ описывает четкий контракт между ViewModel и UI для управления состоянием презентационного слоя в приложении Amulet. Этот контракт основан на архитектуре MVVM+ (MVVM с практиками MVI) и обеспечивает предсказуемый однонаправленный поток данных (UDF).

## 1. Архитектурные принципы

### 1.1. Однонаправленный поток данных (UDF)

```
UI Action (Event) → ViewModel → UseCase → Repository → Data Source
                    ↓
UI State ← StateFlow<ScreenState> ← ViewModel ← Domain Result
```

**Ключевые принципы:**
- UI никогда не изменяет состояние напрямую
- ViewModel является единственным источником истины для UI
- Все изменения состояния проходят через ViewModel
- Состояние иммутабельно и обновляется через копирование

### 1.2. Разделение ответственности

| Компонент | Ответственность |
|-----------|----------------|
| **UI (Compose)** | Отображение состояния, обработка пользовательских действий |
| **ViewModel** | Управление состоянием экрана, обработка событий, координация с UseCase |
| **UseCase** | Бизнес-логика, координация между репозиториями |
| **Repository** | Доступ к данным, кэширование, синхронизация |

## 2. Моделирование состояния экрана

### 2.1. Структура ScreenState

Каждый экран имеет свою модель состояния, которая содержит все необходимые данные для отображения:

```kotlin
// Базовый интерфейс для всех состояний экранов
interface ScreenState {
    val isLoading: Boolean
    val error: AppError?
}

// Пример состояния экрана профиля
data class ProfileState(
    override val isLoading: Boolean = false,
    override val error: AppError? = null,
    val user: User? = null,
    val isEditing: Boolean = false,
    val editedName: String = "",
    val editedTimezone: String = "",
    val consents: UserConsents? = null
) : ScreenState

// Пример состояния экрана объятий
data class HugsState(
    override val isLoading: Boolean = false,
    override val error: AppError? = null,
    val sentHugs: List<Hug> = emptyList(),
    val receivedHugs: List<Hug> = emptyList(),
    val selectedTab: HugsTab = HugsTab.RECEIVED,
    val isSending: Boolean = false,
    val sendProgress: SendProgress? = null
) : ScreenState

// Пример состояния экрана практик
data class PracticesState(
    override val isLoading: Boolean = false,
    override val error: AppError? = null,
    val practices: List<Practice> = emptyList(),
    val categories: List<PracticeCategory> = emptyList(),
    val selectedCategory: PracticeCategory? = null,
    val searchQuery: String = "",
    val activeSession: PracticeSession? = null,
    val sessionProgress: SessionProgress? = null
) : ScreenState
```

### 2.2. Принципы моделирования состояния

**1. Иммутабельность:**
```kotlin
// ✅ Правильно - создание нового состояния
fun updateUser(newUser: User) {
    _uiState.value = _uiState.value.copy(
        user = newUser,
        isLoading = false,
        error = null
    )
}

// ❌ Неправильно - мутация существующего состояния
fun updateUser(newUser: User) {
    _uiState.value.user = newUser // Мутация запрещена
}
```

**2. Полнота данных:**
```kotlin
// ✅ Правильно - все необходимые данные в состоянии
data class DeviceState(
    val device: Device? = null,
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val batteryLevel: Int? = null,
    val isCharging: Boolean = false,
    val firmwareVersion: String? = null,
    val updateAvailable: Boolean = false,
    val otaProgress: OtaProgress? = null
)

// ❌ Неправильно - недостаточно данных для UI
data class DeviceState(
    val device: Device? = null
)
```

**3. Явное представление состояний загрузки:**
```kotlin
// ✅ Правильно - разные флаги для разных операций
data class PatternsState(
    val isLoadingPatterns: Boolean = false,
    val isUploadingPattern: Boolean = false,
    val isDeletingPattern: Boolean = false,
    val patterns: List<Pattern> = emptyList()
)

// ❌ Неправильно - один флаг для всех операций
data class PatternsState(
    val isLoading: Boolean = false, // Неясно, что именно загружается
    val patterns: List<Pattern> = emptyList()
)
```

## 3. Обработка действий пользователя (Events)

### 3.1. Моделирование событий

События пользователя моделируются как sealed классы для типобезопасности:

```kotlin
// Базовый интерфейс для всех событий
interface UiEvent

// Пример событий экрана профиля
sealed interface ProfileEvent : UiEvent {
    data object LoadProfile : ProfileEvent
    data object StartEditing : ProfileEvent
    data object CancelEditing : ProfileEvent
    data object SaveChanges : ProfileEvent
    data class UpdateName(val name: String) : ProfileEvent
    data class UpdateTimezone(val timezone: String) : ProfileEvent
    data class UpdateConsent(val type: ConsentType, val granted: Boolean) : ProfileEvent
    data object Retry : ProfileEvent
}

// Пример событий экрана объятий
sealed interface HugsEvent : UiEvent {
    data object LoadHugs : HugsEvent
    data object RefreshHugs : HugsEvent
    data class SelectTab(val tab: HugsTab) : HugsEvent
    data class SendHug(val recipientId: String, val patternId: String?) : HugsEvent
    data class RetrySendHug(val hugId: String) : HugsEvent
    data class CancelSendHug(val hugId: String) : HugsEvent
    data class MarkAsRead(val hugId: String) : HugsEvent
    data object Retry : HugsEvent
}

// Пример событий экрана практик
sealed interface PracticesEvent : UiEvent {
    data object LoadPractices : PracticesEvent
    data object RefreshPractices : PracticesEvent
    data class SelectCategory(val category: PracticeCategory?) : PracticesEvent
    data class UpdateSearchQuery(val query: String) : PracticesEvent
    data class StartPractice(val practiceId: String) : PracticesEvent
    data class StopPractice(val sessionId: String) : PracticesEvent
    data class PausePractice(val sessionId: String) : PracticesEvent
    data class ResumePractice(val sessionId: String) : PracticesEvent
    data object Retry : PracticesEvent
}
```

### 3.2. Обработка событий в ViewModel

```kotlin
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileState(isLoading = true))
    val uiState: StateFlow<ProfileState> = _uiState.asStateFlow()

    fun handleEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.LoadProfile -> loadProfile()
            is ProfileEvent.StartEditing -> startEditing()
            is ProfileEvent.CancelEditing -> cancelEditing()
            is ProfileEvent.SaveChanges -> saveChanges()
            is ProfileEvent.UpdateName -> updateName(event.name)
            is ProfileEvent.UpdateTimezone -> updateTimezone(event.timezone)
            is ProfileEvent.UpdateConsent -> updateConsent(event.type, event.granted)
            is ProfileEvent.Retry -> retry()
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            getUserProfileUseCase()
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = user,
                        editedName = user.displayName ?: "",
                        editedTimezone = user.timezone ?: "",
                        consents = user.consents
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error
                    )
                }
        }
    }

    private fun startEditing() {
        _uiState.value = _uiState.value.copy(isEditing = true)
    }

    private fun cancelEditing() {
        val currentUser = _uiState.value.user
        _uiState.value = _uiState.value.copy(
            isEditing = false,
            editedName = currentUser?.displayName ?: "",
            editedTimezone = currentUser?.timezone ?: ""
        )
    }

    private fun saveChanges() {
        val currentState = _uiState.value
        if (!currentState.isEditing) return

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)

            val updateRequest = UpdateUserProfileRequest(
                displayName = currentState.editedName,
                timezone = currentState.editedTimezone
            )

            updateUserProfileUseCase(updateRequest)
                .onSuccess { updatedUser ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isEditing = false,
                        user = updatedUser
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error
                    )
                }
        }
    }

    private fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(editedName = name)
    }

    private fun updateTimezone(timezone: String) {
        _uiState.value = _uiState.value.copy(editedTimezone = timezone)
    }

    private fun updateConsent(type: ConsentType, granted: Boolean) {
        val currentConsents = _uiState.value.consents ?: return
        val updatedConsents = when (type) {
            ConsentType.ANALYTICS -> currentConsents.copy(analytics = granted)
            ConsentType.USAGE -> currentConsents.copy(usage = granted)
            ConsentType.CRASH -> currentConsents.copy(crash = granted)
            ConsentType.DIAGNOSTICS -> currentConsents.copy(diagnostics = granted)
        }
        _uiState.value = _uiState.value.copy(consents = updatedConsents)
    }

    private fun retry() {
        loadProfile()
    }
}
```

## 4. Одноразовые события (Side Effects)

### 4.1. Моделирование Side Effects

Side Effects используются для одноразовых событий, которые не должны сохраняться в состоянии:

```kotlin
// Базовый интерфейс для всех side effects
interface SideEffect

// Пример side effects для экрана профиля
sealed interface ProfileSideEffect : SideEffect {
    data class ShowSnackbar(val message: String) : ProfileSideEffect
    data class NavigateToSettings : ProfileSideEffect
    data class ShowConfirmationDialog(val message: String) : ProfileSideEffect
}

// Пример side effects для экрана объятий
sealed interface HugsSideEffect : SideEffect {
    data class ShowSnackbar(val message: String) : HugsSideEffect
    data class NavigateToSendHug(val recipientId: String) : HugsSideEffect
    data class ShowHugDetails(val hugId: String) : HugsSideEffect
    data class ShowRateLimitDialog(val retryAfter: Long) : HugsSideEffect
}

// Пример side effects для экрана практик
sealed interface PracticesSideEffect : SideEffect {
    data class ShowSnackbar(val message: String) : PracticesSideEffect
    data class NavigateToPracticeDetails(val practiceId: String) : PracticesSideEffect
    data class ShowSessionCompleteDialog(val session: PracticeSession) : PracticesSideEffect
    data class ShowBleConnectionRequired : PracticesSideEffect
}
```

### 4.2. Обработка Side Effects в ViewModel

```kotlin
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileState(isLoading = true))
    val uiState: StateFlow<ProfileState> = _uiState.asStateFlow()

    // Side effects как SharedFlow с replay = 0
    private val _sideEffects = MutableSharedFlow<ProfileSideEffect>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val sideEffects: SharedFlow<ProfileSideEffect> = _sideEffects.asSharedFlow()

    private fun saveChanges() {
        val currentState = _uiState.value
        if (!currentState.isEditing) return

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)

            val updateRequest = UpdateUserProfileRequest(
                displayName = currentState.editedName,
                timezone = currentState.editedTimezone
            )

            updateUserProfileUseCase(updateRequest)
                .onSuccess { updatedUser ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isEditing = false,
                        user = updatedUser
                    )
                    // Показываем успешное сообщение
                    _sideEffects.emit(ProfileSideEffect.ShowSnackbar("Профиль успешно обновлен"))
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error
                    )
                    // Показываем сообщение об ошибке
                    _sideEffects.emit(ProfileSideEffect.ShowSnackbar("Ошибка при обновлении профиля"))
                }
        }
    }

    private fun navigateToSettings() {
        _sideEffects.emit(ProfileSideEffect.NavigateToSettings)
    }
}
```

### 4.3. Обработка Side Effects в UI

```kotlin
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Обработка side effects
    LaunchedEffect(viewModel) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is ProfileSideEffect.ShowSnackbar -> {
                    // Показать snackbar
                    // Реализация зависит от выбранного подхода к snackbar
                }
                is ProfileSideEffect.NavigateToSettings -> {
                    onNavigateToSettings()
                }
                is ProfileSideEffect.ShowConfirmationDialog -> {
                    // Показать диалог подтверждения
                }
            }
        }
    }

    // UI компоненты
    ProfileContent(
        state = uiState,
        onEvent = viewModel::handleEvent
    )
}
```

## 5. Специфичные паттерны для Amulet App

### 5.1. Состояние BLE подключения

```kotlin
data class DeviceConnectionState(
    override val isLoading: Boolean = false,
    override val error: AppError? = null,
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val device: Device? = null,
    val batteryLevel: Int? = null,
    val isCharging: Boolean = false,
    val firmwareVersion: String? = null,
    val updateAvailable: Boolean = false,
    val otaProgress: OtaProgress? = null,
    val isConnecting: Boolean = false,
    val isDisconnecting: Boolean = false
) : ScreenState

sealed interface DeviceConnectionEvent : UiEvent {
    data object Connect : DeviceConnectionEvent
    data object Disconnect : DeviceConnectionEvent
    data object RetryConnection : DeviceConnectionEvent
    data object StartOtaUpdate : DeviceConnectionEvent
    data object CancelOtaUpdate : DeviceConnectionEvent
    data object Retry : DeviceConnectionEvent
}

sealed interface DeviceConnectionSideEffect : SideEffect {
    data class ShowSnackbar(val message: String) : DeviceConnectionSideEffect
    data class ShowOtaProgressDialog(val progress: OtaProgress) : DeviceConnectionSideEffect
    data class ShowConnectionErrorDialog(val error: AppError) : DeviceConnectionSideEffect
}
```

### 5.2. Состояние отправки объятий

```kotlin
data class SendHugState(
    override val isLoading: Boolean = false,
    override val error: AppError? = null,
    val recipients: List<User> = emptyList(),
    val selectedRecipient: User? = null,
    val patterns: List<Pattern> = emptyList(),
    val selectedPattern: Pattern? = null,
    val customMessage: String = "",
    val isSending: Boolean = false,
    val sendProgress: SendProgress? = null,
    val queuedHugs: List<QueuedHug> = emptyList()
) : ScreenState

sealed interface SendHugEvent : UiEvent {
    data object LoadRecipients : SendHugEvent
    data object LoadPatterns : SendHugEvent
    data class SelectRecipient(val user: User) : SendHugEvent
    data class SelectPattern(val pattern: Pattern) : SendHugEvent
    data class UpdateCustomMessage(val message: String) : SendHugEvent
    data object SendHug : SendHugEvent
    data class RetrySendHug(val hugId: String) : SendHugEvent
    data class CancelSendHug(val hugId: String) : SendHugEvent
    data object Retry : SendHugEvent
}

sealed interface SendHugSideEffect : SideEffect {
    data class ShowSnackbar(val message: String) : SendHugSideEffect
    data class ShowRateLimitDialog(val retryAfter: Long) : SendHugSideEffect
    data class ShowSendSuccessDialog(val hug: Hug) : SendHugSideEffect
    data class NavigateBack : SendHugSideEffect
}
```

### 5.3. Состояние практик с BLE

```kotlin
data class PracticeSessionState(
    override val isLoading: Boolean = false,
    override val error: AppError? = null,
    val practice: Practice? = null,
    val session: PracticeSession? = null,
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val progress: SessionProgress? = null,
    val deviceConnected: Boolean = false,
    val deviceBatteryLevel: Int? = null,
    val bleError: AppError? = null,
    val isStarting: Boolean = false,
    val isStopping: Boolean = false,
    val isPausing: Boolean = false,
    val isResuming: Boolean = false
) : ScreenState

sealed interface PracticeSessionEvent : UiEvent {
    data class StartSession(val practiceId: String) : PracticeSessionEvent
    data object PauseSession : PracticeSessionEvent
    data object ResumeSession : PracticeSessionEvent
    data object StopSession : PracticeSessionEvent
    data object RetryBleConnection : PracticeSessionEvent
    data object Retry : PracticeSessionEvent
}

sealed interface PracticeSessionSideEffect : SideEffect {
    data class ShowSnackbar(val message: String) : PracticeSessionSideEffect
    data class ShowBleConnectionRequired : PracticeSessionSideEffect
    data class ShowSessionCompleteDialog(val session: PracticeSession) : PracticeSessionSideEffect
    data class ShowBleErrorDialog(val error: AppError) : PracticeSessionSideEffect
    data class NavigateToDeviceSettings : PracticeSessionSideEffect
}
```

## 6. Обработка ошибок в UI

### 6.1. Централизованная обработка ошибок

```kotlin
@Composable
fun ErrorHandler(
    error: AppError?,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    error?.let { appError ->
        when (appError) {
            is AppError.Network -> {
                NetworkErrorDialog(
                    onRetry = onRetry,
                    onDismiss = onDismiss
                )
            }
            is AppError.BleError -> {
                BleErrorDialog(
                    error = appError,
                    onRetry = onRetry,
                    onDismiss = onDismiss
                )
            }
            is AppError.Validation -> {
                ValidationErrorSnackbar(
                    errors = appError.errors,
                    onDismiss = onDismiss
                )
            }
            is AppError.RateLimited -> {
                RateLimitDialog(
                    onRetry = onRetry,
                    onDismiss = onDismiss
                )
            }
            else -> {
                GenericErrorDialog(
                    error = appError,
                    onRetry = onRetry,
                    onDismiss = onDismiss
                )
            }
        }
    }
}
```

### 6.2. Интеграция с экранами

```kotlin
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Обработка ошибок
    ErrorHandler(
        error = uiState.error,
        onRetry = { viewModel.handleEvent(ProfileEvent.Retry) },
        onDismiss = { 
            // Очистить ошибку из состояния
            viewModel.handleEvent(ProfileEvent.DismissError)
        }
    )

    // Основной контент
    ProfileContent(
        state = uiState,
        onEvent = viewModel::handleEvent
    )
}
```

## 7. Тестирование ViewModel

### 7.1. Unit тесты для ViewModel

```kotlin
class ProfileViewModelTest {
    
    @Test
    fun `should load profile successfully`() = runTest {
        // Given
        val user = User(id = "test-user", displayName = "Test User")
        val useCase = mockk<GetUserProfileUseCase> {
            coEvery { invoke() } returns Result.success(user)
        }
        val viewModel = ProfileViewModel(useCase, mockk())

        // When
        viewModel.handleEvent(ProfileEvent.LoadProfile)

        // Then
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.user).isEqualTo(user)
        assertThat(state.error).isNull()
    }

    @Test
    fun `should handle profile load error`() = runTest {
        // Given
        val error = AppError.Network
        val useCase = mockk<GetUserProfileUseCase> {
            coEvery { invoke() } returns Result.failure(error)
        }
        val viewModel = ProfileViewModel(useCase, mockk())

        // When
        viewModel.handleEvent(ProfileEvent.LoadProfile)

        // Then
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.user).isNull()
        assertThat(state.error).isEqualTo(error)
    }

    @Test
    fun `should emit side effect on successful save`() = runTest {
        // Given
        val updatedUser = User(id = "test-user", displayName = "Updated Name")
        val updateUseCase = mockk<UpdateUserProfileUseCase> {
            coEvery { invoke(any()) } returns Result.success(updatedUser)
        }
        val viewModel = ProfileViewModel(mockk(), updateUseCase)
        
        // Start editing
        viewModel.handleEvent(ProfileEvent.StartEditing)
        viewModel.handleEvent(ProfileEvent.UpdateName("Updated Name"))

        // When
        viewModel.handleEvent(ProfileEvent.SaveChanges)

        // Then
        val sideEffects = viewModel.sideEffects.take(1).toList()
        assertThat(sideEffects).hasSize(1)
        assertThat(sideEffects[0]).isInstanceOf(ProfileSideEffect.ShowSnackbar::class.java)
    }
}
```

### 7.2. Тестирование с Turbine

```kotlin
class ProfileViewModelTurbineTest {
    
    @Test
    fun `should emit correct state flow`() = runTest {
        // Given
        val user = User(id = "test-user", displayName = "Test User")
        val useCase = mockk<GetUserProfileUseCase> {
            coEvery { invoke() } returns Result.success(user)
        }
        val viewModel = ProfileViewModel(useCase, mockk())

        // When & Then
        viewModel.uiState.test {
            // Initial state
            assertThat(awaitItem().isLoading).isTrue()
            
            // Load profile
            viewModel.handleEvent(ProfileEvent.LoadProfile)
            
            // Final state
            val finalState = awaitItem()
            assertThat(finalState.isLoading).isFalse()
            assertThat(finalState.user).isEqualTo(user)
            assertThat(finalState.error).isNull()
        }
    }
}
```

## 8. Рекомендации по реализации

### 8.1. Лучшие практики

**1. Используйте data class для состояний:**
```kotlin
// ✅ Правильно
data class ProfileState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: AppError? = null
)

// ❌ Неправильно
class ProfileState {
    var isLoading: Boolean = false
    var user: User? = null
    var error: AppError? = null
}
```

**2. Группируйте связанные поля:**
```kotlin
// ✅ Правильно
data class PracticesState(
    val isLoading: Boolean = false,
    val practices: List<Practice> = emptyList(),
    val selectedCategory: PracticeCategory? = null,
    val searchQuery: String = "",
    val sessionState: SessionState = SessionState.Inactive
)

// ❌ Неправильно - разбросанные поля
data class PracticesState(
    val isLoading: Boolean = false,
    val practices: List<Practice> = emptyList(),
    val isSessionActive: Boolean = false,
    val sessionProgress: Float = 0f,
    val sessionDuration: Long = 0L,
    val selectedCategory: PracticeCategory? = null
)
```

**3. Используйте sealed классы для событий:**
```kotlin
// ✅ Правильно
sealed interface ProfileEvent : UiEvent {
    data object LoadProfile : ProfileEvent
    data class UpdateName(val name: String) : ProfileEvent
}

// ❌ Неправильно
interface ProfileEvent {
    fun loadProfile()
    fun updateName(name: String)
}
```

### 8.2. Антипаттерны

**1. Не мутируйте состояние напрямую:**
```kotlin
// ❌ Неправильно
fun updateUser(newUser: User) {
    _uiState.value.user = newUser // Мутация запрещена
}

// ✅ Правильно
fun updateUser(newUser: User) {
    _uiState.value = _uiState.value.copy(user = newUser)
}
```

**2. Не храните UI-специфичные данные в состоянии:**
```kotlin
// ❌ Неправильно
data class ProfileState(
    val user: User? = null,
    val snackbarMessage: String? = null, // UI-специфично
    val showDialog: Boolean = false // UI-специфично
)

// ✅ Правильно
data class ProfileState(
    val user: User? = null
)

// Side effects для UI-специфичных данных
sealed interface ProfileSideEffect {
    data class ShowSnackbar(val message: String) : ProfileSideEffect
    data object ShowDialog : ProfileSideEffect
}
```

**3. Не используйте один флаг для разных операций:**
```kotlin
// ❌ Неправильно
data class PatternsState(
    val isLoading: Boolean = false, // Неясно, что загружается
    val patterns: List<Pattern> = emptyList()
)

// ✅ Правильно
data class PatternsState(
    val isLoadingPatterns: Boolean = false,
    val isUploadingPattern: Boolean = false,
    val isDeletingPattern: Boolean = false,
    val patterns: List<Pattern> = emptyList()
)
```

## 9. Интеграция с архитектурой

### 9.1. Связь с Domain Layer

```kotlin
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val updateUserConsentsUseCase: UpdateUserConsentsUseCase
) : ViewModel() {

    fun handleEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.LoadProfile -> loadProfile()
            is ProfileEvent.UpdateConsent -> updateConsent(event.type, event.granted)
            // ... другие события
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            getUserProfileUseCase()
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = user,
                        error = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error
                    )
                }
        }
    }
}
```

### 9.2. Интеграция с DI

```kotlin
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
) : ViewModel() {
    // ... реализация
}

// В UI
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    // ... использование
}
```

## 10. Заключение

Данный контракт между ViewModel и UI обеспечивает:

1. **Предсказуемость** - четкий поток данных и обработка событий
2. **Тестируемость** - легко тестируемые компоненты с четкими границами
3. **Масштабируемость** - легко добавлять новые экраны и функции
4. **Поддерживаемость** - понятная структура и разделение ответственности
5. **Типобезопасность** - использование sealed классов для событий и side effects

Этот подход экономит время на этапе реализации UI, обеспечивая четкие контракты и предсказуемое поведение компонентов.
