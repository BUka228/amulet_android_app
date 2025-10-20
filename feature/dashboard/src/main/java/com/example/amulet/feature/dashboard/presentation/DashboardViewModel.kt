package com.example.amulet.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.core.logging.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для Dashboard экрана.
 * Следует паттерну MVVM+MVI с Unidirectional Data Flow.
 * 
 * Использует заглушки для данных — UseCase и Repository не реализованы.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    // TODO: Inject UseCases when implemented
    // private val getDeviceStatusUseCase: GetDeviceStatusUseCase,
    // private val getDailyStatsUseCase: GetDailyStatsUseCase,
    // private val startPracticeUseCase: StartPracticeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _sideEffects = MutableSharedFlow<DashboardSideEffect>()
    val sideEffects: SharedFlow<DashboardSideEffect> = _sideEffects.asSharedFlow()

    init {
        loadDashboardData()
    }

    /**
     * Единая точка обработки UI событий.
     * Следует паттерну MVI - все действия проходят через этот метод.
     */
    fun handleEvent(event: DashboardUiEvent) {
        Logger.d("handleEvent: $event", TAG)
        when (event) {
            DashboardUiEvent.Refresh -> loadDashboardData()
            is DashboardUiEvent.StartPractice -> startPractice(event.practiceId)
            DashboardUiEvent.NavigateToPairing -> navigateToPairing()
            DashboardUiEvent.NavigateToLibrary -> navigateToLibrary()
            DashboardUiEvent.NavigateToHugs -> navigateToHugs()
            DashboardUiEvent.NavigateToPatterns -> navigateToPatterns()
            DashboardUiEvent.NavigateToSettings -> navigateToSettings()
            DashboardUiEvent.ErrorConsumed -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // TODO: Replace with real UseCases
            // val deviceResult = getDeviceStatusUseCase()
            // val statsResult = getDailyStatsUseCase(LocalDate.now())
            
            // Mock data для разработки
            _uiState.update {
                it.copy(
                    isLoading = false,
                    userName = "Александр",
                    deviceStatus = DeviceStatus(
                        name = "Amulet AMU-200",
                        connectionStatus = "connected",
                        batteryLevel = 85,
                        currentAnimation = "Pulse"
                    ),
                    dailyStats = DailyStats(
                        practiceMinutes = 42,
                        hugsCount = 5,
                        calmLevel = 75
                    )
                )
            }

            Logger.i("Dashboard data loaded", TAG)
        }
    }

    private fun startPractice(practiceId: String) {
        viewModelScope.launch {
            Logger.d("Starting practice: $practiceId", TAG)
            
            // TODO: Implement with StartPracticeUseCase
            // val result = startPracticeUseCase(practiceId)
            // result.fold(
            //     success = { sessionId ->
            //         _sideEffects.emit(DashboardSideEffect.StartPracticeSession(sessionId))
            //     },
            //     failure = { error ->
            //         _uiState.update { it.copy(error = error) }
            //     }
            // )

            // Заглушка - просто эмитим эффект
            _sideEffects.emit(DashboardSideEffect.StartPracticeSession(practiceId))
            _sideEffects.emit(DashboardSideEffect.ShowToast("Запуск практики: $practiceId"))
        }
    }

    private fun navigateToPairing() {
        viewModelScope.launch {
            Logger.d("Navigate to Pairing", TAG)
            _sideEffects.emit(DashboardSideEffect.NavigateToPairing)
        }
    }

    private fun navigateToLibrary() {
        viewModelScope.launch {
            Logger.d("Navigate to Library", TAG)
            _sideEffects.emit(DashboardSideEffect.NavigateToLibrary)
        }
    }

    private fun navigateToHugs() {
        viewModelScope.launch {
            Logger.d("Navigate to Hugs", TAG)
            _sideEffects.emit(DashboardSideEffect.NavigateToHugs)
        }
    }

    private fun navigateToPatterns() {
        viewModelScope.launch {
            Logger.d("Navigate to Patterns", TAG)
            _sideEffects.emit(DashboardSideEffect.NavigateToPatterns)
        }
    }

    private fun navigateToSettings() {
        viewModelScope.launch {
            Logger.d("Navigate to Settings", TAG)
            _sideEffects.emit(DashboardSideEffect.NavigateToSettings)
        }
    }

    private companion object {
        private const val TAG = "DashboardViewModel"
    }
}
