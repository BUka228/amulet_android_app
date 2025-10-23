package com.example.amulet.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.core.logging.Logger
import com.example.amulet.shared.domain.devices.model.ConnectionStatus
import com.example.amulet.shared.domain.devices.usecase.ObserveConnectionStateUseCase
import com.example.amulet.shared.domain.devices.usecase.ObserveDevicesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для Dashboard экрана.
 * Следует паттерну MVVM+MVI с Unidirectional Data Flow.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val observeDevicesUseCase: ObserveDevicesUseCase,
    private val observeConnectionStateUseCase: ObserveConnectionStateUseCase
    // TODO: Inject practice UseCases when implemented
    // private val getDailyStatsUseCase: GetDailyStatsUseCase,
    // private val startPracticeUseCase: StartPracticeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _sideEffects = MutableSharedFlow<DashboardSideEffect>()
    val sideEffects: SharedFlow<DashboardSideEffect> = _sideEffects.asSharedFlow()

    init {
        loadDashboardData()
        observeDevices()
        observeConnectionState()
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
            is DashboardUiEvent.DeviceClicked -> navigateToDeviceDetails(event.deviceId)
            DashboardUiEvent.NavigateToDevicesList -> navigateToDevicesList()
            DashboardUiEvent.NavigateToPairing -> navigateToPairing()
            DashboardUiEvent.NavigateToLibrary -> navigateToLibrary()
            DashboardUiEvent.NavigateToHugs -> navigateToHugs()
            DashboardUiEvent.NavigateToPatterns -> navigateToPatterns()
            DashboardUiEvent.NavigateToSettings -> navigateToSettings()
            DashboardUiEvent.ErrorConsumed -> _uiState.update { it.copy(error = null) }
        }
    }
    
    private fun observeDevices() {
        observeDevicesUseCase()
            .onEach { devices ->
                _uiState.update { it.copy(devices = devices) }
                Logger.d("Devices updated: ${devices.size}", TAG)
            }
            .launchIn(viewModelScope)
    }
    
    private fun observeConnectionState() {
        observeConnectionStateUseCase()
            .onEach { connectionStatus ->
                val connectedDevice = if (connectionStatus == ConnectionStatus.CONNECTED) {
                    _uiState.value.devices.firstOrNull()
                } else null
                _uiState.update { it.copy(connectedDevice = connectedDevice) }
                Logger.d("Connection status: $connectionStatus", TAG)
            }
            .launchIn(viewModelScope)
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // TODO: Replace with real UseCases
            // val deviceResult = getDeviceStatusUseCase()
            // val statsResult = getDailyStatsUseCase(LocalDate.now())
            
            // Mock data для статистики
            _uiState.update {
                it.copy(
                    isLoading = false,
                    userName = "Александр",
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
            _sideEffects.emit(DashboardSideEffect.StartPracticeSession(practiceId))
            _sideEffects.emit(DashboardSideEffect.ShowToast("Запуск практики: $practiceId"))
        }
    }

    private fun navigateToDeviceDetails(deviceId: String) {
        viewModelScope.launch {
            Logger.d("Navigate to Device Details: $deviceId", TAG)
            _sideEffects.emit(DashboardSideEffect.NavigateToDeviceDetails(deviceId))
        }
    }

    private fun navigateToDevicesList() {
        viewModelScope.launch {
            Logger.d("Navigate to Devices List", TAG)
            _sideEffects.emit(DashboardSideEffect.NavigateToDevicesList)
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
