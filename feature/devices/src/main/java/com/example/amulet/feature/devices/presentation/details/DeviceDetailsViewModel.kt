package com.example.amulet.feature.devices.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.domain.devices.model.DeviceId
import com.example.amulet.shared.domain.devices.usecase.CheckFirmwareUpdateUseCase
import com.example.amulet.shared.domain.devices.usecase.GetDeviceUseCase
import com.example.amulet.shared.domain.devices.usecase.RemoveDeviceUseCase
import com.example.amulet.shared.domain.devices.usecase.UpdateDeviceSettingsUseCase
import com.example.amulet.shared.domain.devices.usecase.ApplyDeviceBrightnessUseCase
import com.example.amulet.shared.domain.devices.usecase.ApplyDeviceHapticsUseCase
import com.example.amulet.shared.domain.devices.usecase.ObserveDeviceSessionStatusUseCase
import com.example.amulet.shared.domain.devices.usecase.ConnectToDeviceUseCase
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
    private val removeDeviceUseCase: RemoveDeviceUseCase,
    private val checkFirmwareUpdateUseCase: CheckFirmwareUpdateUseCase,
    private val applyDeviceBrightnessUseCase: ApplyDeviceBrightnessUseCase,
    private val applyDeviceHapticsUseCase: ApplyDeviceHapticsUseCase,
    private val connectToDeviceUseCase: ConnectToDeviceUseCase,
    private val observeDeviceSessionStatusUseCase: ObserveDeviceSessionStatusUseCase,
) : ViewModel() {

    private val deviceId: String = checkNotNull(savedStateHandle["deviceId"])

    private val _uiState = MutableStateFlow(DeviceDetailsState(isLoading = true))
    val uiState: StateFlow<DeviceDetailsState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<DeviceDetailsSideEffect>()
    val sideEffect: SharedFlow<DeviceDetailsSideEffect> = _sideEffect.asSharedFlow()

    init {
        loadDevice()
        checkFirmwareUpdate()
        observeDeviceSession()
    }

    private fun observeDeviceSession() {
        viewModelScope.launch {
            observeDeviceSessionStatusUseCase().collect { sessionStatus ->
                val isOnline = sessionStatus.connection is com.example.amulet.shared.domain.devices.model.BleConnectionState.Connected ||
                    (sessionStatus.liveStatus?.isOnline == true)
                _uiState.update { state ->
                    state.copy(
                        isDeviceOnline = isOnline,
                        batteryLevel = sessionStatus.liveStatus?.batteryLevel
                    )
                }
            }
        }
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
            is DeviceDetailsEvent.SaveSettings -> saveSettings(event.name)
            is DeviceDetailsEvent.Reconnect -> reconnect()
        }
    }

    private fun reconnect() {
        val currentDevice = _uiState.value.device ?: return
        viewModelScope.launch {
            // Просто триггерим use case; состояние подключения UI читает через ObserveDeviceSessionStatusUseCase
            connectToDeviceUseCase(currentDevice.bleAddress).first()
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

    private fun saveSettings(name: String) {
        val currentDevice = _uiState.value.device ?: return
        val brightness = currentDevice.settings.brightness
        val haptics = currentDevice.settings.haptics
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            // Сначала сохраняем все настройки в БД
            updateDeviceSettingsUseCase(
                deviceId = currentDevice.id,
                name = name,
                brightness = brightness,
                haptics = haptics
            )
                .onSuccess { updatedDevice ->
                    _uiState.update { it.copy(device = updatedDevice) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = error
                        )
                    }
                    return@launch
                }
        
            // Затем отправляем яркость и вибрацию на устройство
            val applyBrightnessResult = applyDeviceBrightnessUseCase(currentDevice.id, brightness)
            val applyHapticsResult = applyDeviceHapticsUseCase(currentDevice.id, haptics)
        
            val applyError = applyBrightnessResult.component2() ?: applyHapticsResult.component2()
            _uiState.update {
                it.copy(
                    isSaving = false,
                    error = applyError
                )
            }
        }
    }

    private fun unclaimDevice() {
        val currentDevice = _uiState.value.device ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            removeDeviceUseCase(currentDevice.id)
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
