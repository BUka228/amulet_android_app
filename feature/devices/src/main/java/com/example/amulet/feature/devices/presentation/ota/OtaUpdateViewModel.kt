package com.example.amulet.feature.devices.presentation.ota

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.devices.model.DeviceId
import com.example.amulet.shared.domain.devices.model.FirmwareUpdate
import com.example.amulet.shared.domain.devices.model.OtaUpdateProgress
import com.example.amulet.shared.domain.devices.model.OtaUpdateState as OtaProgressStage
import com.example.amulet.shared.domain.devices.usecase.CancelOtaUpdateUseCase
import com.example.amulet.shared.domain.devices.usecase.CheckFirmwareUpdateUseCase
import com.example.amulet.shared.domain.devices.usecase.StartBleOtaUpdateUseCase
import com.example.amulet.shared.domain.devices.usecase.StartWifiOtaUpdateUseCase
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtaUpdateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val checkFirmwareUpdateUseCase: CheckFirmwareUpdateUseCase,
    private val startBleOtaUpdateUseCase: StartBleOtaUpdateUseCase,
    private val startWifiOtaUpdateUseCase: StartWifiOtaUpdateUseCase,
    private val cancelOtaUpdateUseCase: CancelOtaUpdateUseCase
) : ViewModel() {

    private val deviceId: String = checkNotNull(savedStateHandle["deviceId"])

    private val _uiState = MutableStateFlow(OtaUpdateState(isLoading = true))
    val uiState: StateFlow<OtaUpdateState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<OtaUpdateSideEffect>()
    val sideEffect: SharedFlow<OtaUpdateSideEffect> = _sideEffect.asSharedFlow()

    init {
        checkForUpdate()
    }

    fun handleEvent(event: OtaUpdateEvent) {
        when (event) {
            is OtaUpdateEvent.StartBleUpdate -> startBleUpdate()
            is OtaUpdateEvent.StartWifiUpdate -> startWifiUpdate(event.ssid, event.password)
            is OtaUpdateEvent.CancelUpdate -> cancelUpdate()
            is OtaUpdateEvent.NavigateBack -> {
                viewModelScope.launch {
                    _sideEffect.emit(OtaUpdateSideEffect.NavigateBack)
                }
            }
            is OtaUpdateEvent.DismissError -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }

    private fun checkForUpdate() {
        viewModelScope.launch {
            checkFirmwareUpdateUseCase(DeviceId(deviceId))
                .onSuccess { firmwareUpdate ->
                    _uiState.update { 
                        it.copy(
                            firmwareUpdate = firmwareUpdate,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = error
                        )
                    }
                }
        }
    }

    private fun startBleUpdate() {
        val update = _uiState.value.firmwareUpdate ?: return
        
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isUpdating = true,
                    updateMethod = OtaUpdateMethod.BLE,
                    error = null
                )
            }

            startBleOtaUpdateUseCase(
                deviceId = DeviceId(deviceId),
                firmwareUpdate = update
            ).collect { progress ->
                handleOtaProgress(progress)
            }
        }
    }

    private fun startWifiUpdate(ssid: String, password: String) {
        val update = _uiState.value.firmwareUpdate ?: return
        
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isUpdating = true,
                    updateMethod = OtaUpdateMethod.WIFI,
                    error = null
                )
            }

            startWifiOtaUpdateUseCase(
                deviceId = DeviceId(deviceId),
                ssid = ssid,
                password = password,
                firmwareUpdate = update
            ).collect { progress ->
                handleOtaProgress(progress)
            }
        }
    }

    private fun handleOtaProgress(progress: OtaUpdateProgress) {
        _uiState.update { 
            it.copy(
                otaProgress = progress,
                error = progress.error?.let { msg -> 
                    AppError.Unknown 
                }
            )
        }

        when (progress.state) {
            OtaProgressStage.COMPLETED -> {
                viewModelScope.launch {
                    _sideEffect.emit(OtaUpdateSideEffect.UpdateCompleted)
                }
            }
            OtaProgressStage.FAILED, OtaProgressStage.CANCELLED -> {
                _uiState.update { it.copy(isUpdating = false) }
            }
            else -> { /* Обновление в процессе */ }
        }
    }

    private fun cancelUpdate() {
        viewModelScope.launch {
            cancelOtaUpdateUseCase()
                .onSuccess {
                    _uiState.update { 
                        it.copy(
                            isUpdating = false,
                            otaProgress = null
                        )
                    }
                }
                .onFailure { /* Игнорируем ошибку отмены */ }
        }
    }
}
