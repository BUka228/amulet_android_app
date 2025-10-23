package com.example.amulet.feature.devices.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.domain.devices.model.DeviceId
import com.example.amulet.shared.domain.devices.usecase.CheckFirmwareUpdateUseCase
import com.example.amulet.shared.domain.devices.usecase.GetDeviceUseCase
import com.example.amulet.shared.domain.devices.usecase.UnclaimDeviceUseCase
import com.example.amulet.shared.domain.devices.usecase.UpdateDeviceSettingsUseCase
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getDeviceUseCase: GetDeviceUseCase,
    private val updateDeviceSettingsUseCase: UpdateDeviceSettingsUseCase,
    private val unclaimDeviceUseCase: UnclaimDeviceUseCase,
    private val checkFirmwareUpdateUseCase: CheckFirmwareUpdateUseCase
) : ViewModel() {

    private val deviceId: String = checkNotNull(savedStateHandle["deviceId"])

    private val _uiState = MutableStateFlow(DeviceDetailsState(isLoading = true))
    val uiState: StateFlow<DeviceDetailsState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<DeviceDetailsSideEffect>()
    val sideEffect: SharedFlow<DeviceDetailsSideEffect> = _sideEffect.asSharedFlow()

    init {
        loadDevice()
        checkFirmwareUpdate()
    }

    fun handleEvent(event: DeviceDetailsEvent) {
        when (event) {
            is DeviceDetailsEvent.UpdateName -> updateDeviceName(event.name)
            is DeviceDetailsEvent.UpdateBrightness -> updateBrightness(event.brightness)
            is DeviceDetailsEvent.UpdateHaptics -> updateHaptics(event.haptics)
            is DeviceDetailsEvent.UnclaimDevice -> unclaimDevice()
            is DeviceDetailsEvent.NavigateToOta -> {
                viewModelScope.launch {
                    _sideEffect.emit(DeviceDetailsSideEffect.NavigateToOta(deviceId))
                }
            }
            is DeviceDetailsEvent.DismissError -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }

    private fun loadDevice() {
        viewModelScope.launch {
            getDeviceUseCase(DeviceId(deviceId))
                .onSuccess { device ->
                    _uiState.update { 
                        it.copy(
                            device = device,
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

    private fun checkFirmwareUpdate() {
        viewModelScope.launch {
            checkFirmwareUpdateUseCase(DeviceId(deviceId))
                .onSuccess { firmwareUpdate ->
                    _uiState.update { 
                        it.copy(firmwareUpdate = firmwareUpdate)
                    }
                }
                .onFailure { /* Игнорируем ошибку проверки обновлений */ }
        }
    }

    private fun updateDeviceName(name: String) {
        val currentDevice = _uiState.value.device ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            updateDeviceSettingsUseCase(
                deviceId = currentDevice.id,
                name = name
            )
                .onSuccess { updatedDevice ->
                    _uiState.update { 
                        it.copy(
                            device = updatedDevice,
                            isSaving = false,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isSaving = false,
                            error = error
                        )
                    }
                }
        }
    }

    private fun updateBrightness(brightness: Double) {
        val currentDevice = _uiState.value.device ?: return
        
        viewModelScope.launch {
            updateDeviceSettingsUseCase(
                deviceId = currentDevice.id,
                brightness = brightness
            )
                .onSuccess { updatedDevice ->
                    _uiState.update { it.copy(device = updatedDevice) }
                }
                .onFailure { /* Игнорируем ошибку для плавности UI */ }
        }
    }

    private fun updateHaptics(haptics: Double) {
        val currentDevice = _uiState.value.device ?: return
        
        viewModelScope.launch {
            updateDeviceSettingsUseCase(
                deviceId = currentDevice.id,
                haptics = haptics
            )
                .onSuccess { updatedDevice ->
                    _uiState.update { it.copy(device = updatedDevice) }
                }
                .onFailure { /* Игнорируем ошибку для плавности UI */ }
        }
    }

    private fun unclaimDevice() {
        val currentDevice = _uiState.value.device ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            unclaimDeviceUseCase(currentDevice.id)
                .onSuccess {
                    _sideEffect.emit(DeviceDetailsSideEffect.DeviceUnclaimedNavigateBack)
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isDeleting = false,
                            error = error
                        )
                    }
                }
        }
    }
}
