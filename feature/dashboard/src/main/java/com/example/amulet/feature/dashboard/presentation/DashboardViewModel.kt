package com.example.amulet.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.core.logging.Logger
import com.example.amulet.shared.domain.dashboard.usecase.GetDashboardDailyStatsUseCase
import com.example.amulet.shared.domain.devices.model.BleConnectionState
import com.example.amulet.shared.domain.devices.usecase.ObserveDevicesUseCase
import com.example.amulet.shared.domain.devices.usecase.ObserveDeviceSessionStatusUseCase
import com.example.amulet.shared.domain.practices.usecase.GetPracticeByIdUseCase
import com.example.amulet.shared.domain.practices.usecase.GetRecommendationsStreamUseCase
import com.example.amulet.shared.domain.practices.usecase.GetSessionsHistoryStreamUseCase
import com.example.amulet.shared.domain.user.usecase.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для Dashboard экрана.
 * Следует паттерну MVVM+MVI с Unidirectional Data Flow.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val observeDevicesUseCase: ObserveDevicesUseCase,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val getDashboardDailyStatsUseCase: GetDashboardDailyStatsUseCase,
    private val getSessionsHistoryStreamUseCase: GetSessionsHistoryStreamUseCase,
    private val getRecommendationsStreamUseCase: GetRecommendationsStreamUseCase,
    private val observeDeviceSessionStatusUseCase: ObserveDeviceSessionStatusUseCase,
    private val getPracticeByIdUseCase: GetPracticeByIdUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _sideEffects = MutableSharedFlow<DashboardSideEffect>()
    val sideEffects: SharedFlow<DashboardSideEffect> = _sideEffects.asSharedFlow()

    private var quickStartPracticeJob: Job? = null

    init {
        observeDashboardStats()
        observeDevicesAndSession()
        observeCurrentUser()
        observeQuickStartPractice()
    }

    /**
     * Единая точка обработки UI событий.
     * Следует паттерну MVI - все действия проходят через этот метод.
     */
    fun handleEvent(event: DashboardUiEvent) {
        Logger.d("handleEvent: $event", TAG)
        when (event) {
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

    private fun observeDevicesAndSession() {
        combine(
            observeDevicesUseCase(),
            observeDeviceSessionStatusUseCase(),
        ) { devices, sessionStatus ->
            Pair(devices, sessionStatus)
        }
            .onEach { (devices, sessionStatus) ->
                val isConnected = sessionStatus.connection is BleConnectionState.Connected
                val connectedDevice = if (isConnected) devices.firstOrNull() else null

                _uiState.update { state ->
                    state.copy(
                        devices = devices,
                        connectedDevice = connectedDevice,
                        connectedBatteryLevel = sessionStatus.liveStatus?.batteryLevel
                    )
                }

                Logger.d(
                    "Dashboard: devices=${'$'}{devices.size}, connection=${'$'}{sessionStatus.connection}, battery=${'$'}{sessionStatus.liveStatus?.batteryLevel}",
                    TAG
                )
            }
            .launchIn(viewModelScope)
    }

    private fun observeQuickStartPractice() {
        combine(
            getSessionsHistoryStreamUseCase(limit = 1),
            getRecommendationsStreamUseCase(limit = 1, goal = null)
        ) { recentSessions, recommendedPractices ->
            val recentPracticeId = recentSessions.firstOrNull()?.practiceId
            val recommendedPracticeId = recommendedPractices.firstOrNull()?.id

            Pair(recentPracticeId, recommendedPracticeId)
        }
            .onEach { (recentPracticeId, recommendedPracticeId) ->
                val quickStartId = recentPracticeId ?: recommendedPracticeId

                _uiState.update {
                    it.copy(
                        quickStartPracticeId = quickStartId,
                        recommendedPracticeId = recommendedPracticeId
                    )
                }

                quickStartPracticeJob?.cancel()
                if (quickStartId != null) {
                    quickStartPracticeJob = viewModelScope.launch {
                        getPracticeByIdUseCase(quickStartId).collect { practice ->
                            _uiState.update {
                                it.copy(
                                    quickStartPracticeTitle = practice?.title,
                                    quickStartPracticeSubtitle = practice?.description
                                )
                            }
                        }
                    }
                } else {
                    quickStartPracticeJob = null
                    _uiState.update {
                        it.copy(
                            quickStartPracticeTitle = null,
                            quickStartPracticeSubtitle = null
                        )
                    }
                }

                Logger.d("Quick start practice updated: recent=$recentPracticeId, recommended=$recommendedPracticeId", TAG)
            }
            .launchIn(viewModelScope)
    }

    private fun observeCurrentUser() {
        observeCurrentUserUseCase()
            .onEach { user ->
                _uiState.update { it.copy(userName = user?.displayName) }
                Logger.d("Current user updated: ${user?.id?.value}", TAG)
            }
            .launchIn(viewModelScope)
    }


    private fun observeDashboardStats() {
        getDashboardDailyStatsUseCase()
            .onEach { stats ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        dailyStats = DailyStats(
                            practiceMinutes = stats.practiceMinutes,
                            hugsCount = stats.hugsCount,
                            calmLevel = stats.calmLevel
                        )
                    )
                }
                Logger.d("Dashboard stats updated: $stats", TAG)
            }
            .launchIn(viewModelScope)
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
