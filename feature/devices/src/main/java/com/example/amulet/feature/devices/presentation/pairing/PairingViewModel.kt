package com.example.amulet.feature.devices.presentation.pairing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.devices.model.DeviceConnectionProgress
import com.example.amulet.shared.domain.devices.usecase.AddDeviceUseCase
import com.example.amulet.shared.domain.devices.usecase.ConnectToDeviceUseCase
import com.example.amulet.shared.domain.devices.usecase.ScanForDevicesUseCase
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для экрана добавления устройства.
 * Упрощенный флоу без QR/NFC - только BLE сканирование.
 */
@HiltViewModel
class PairingViewModel @Inject constructor(
    private val scanForDevicesUseCase: ScanForDevicesUseCase,
    private val connectToDeviceUseCase: ConnectToDeviceUseCase,
    private val addDeviceUseCase: AddDeviceUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(PairingState())
    val state = _state.asStateFlow()
    
    private val _sideEffects = MutableSharedFlow<PairingSideEffect>()
    val sideEffects = _sideEffects.asSharedFlow()
    
    private var scanJob: Job? = null
    private var connectionJob: Job? = null
    
    fun onEvent(event: PairingEvent) {
        when (event) {
            is PairingEvent.StartScanning -> startScanning()
            is PairingEvent.StopScanning -> stopScanning()
            is PairingEvent.SelectDevice -> selectDevice(event.device)
            is PairingEvent.ConnectAndAddDevice -> connectAndAddDevice(event.deviceName)
            is PairingEvent.CancelConnection -> cancelConnection()
            is PairingEvent.DismissError -> dismissError()
            is PairingEvent.NavigateBack -> navigateBack()
        }
    }
    
    private fun startScanning() {
        stopScanning()
        
        _state.update { it.copy(
            isScanning = true,
            foundDevices = emptyList(),
            error = null
        ) }
        
        scanJob = viewModelScope.launch {
            scanForDevicesUseCase(timeoutMs = 30_000L)
                .catch { error ->
                    _state.update { it.copy(
                        isScanning = false,
                        error = com.example.amulet.shared.core.AppError.BleError.DeviceNotFound
                    ) }
                }
                .collect { foundDevice ->
                    _state.update { currentState ->
                        val updatedDevices = currentState.foundDevices
                            .filter { it.bleAddress != foundDevice.bleAddress } + foundDevice
                        currentState.copy(foundDevices = updatedDevices)
                    }
                }
        }
    }
    
    private fun stopScanning() {
        scanJob?.cancel()
        scanJob = null
        _state.update { it.copy(isScanning = false) }
    }
    
    private fun selectDevice(device: com.example.amulet.shared.domain.devices.model.PairingDeviceFound) {
        stopScanning()
        _state.update { it.copy(selectedDevice = device) }
    }
    
    private fun connectAndAddDevice(deviceName: String) {
        val device = _state.value.selectedDevice ?: return
        
        _state.update { it.copy(
            isConnecting = true,
            connectionProgress = "Подключение...",
            error = null
        ) }
        
        connectionJob = viewModelScope.launch {
            // Шаг 1: Подключаемся по BLE
            var connected = false
            var hardwareVersion = device.hardwareVersion ?: 100
            
            connectToDeviceUseCase(device.bleAddress)
                .catch { error ->
                    _state.update { it.copy(
                        isConnecting = false,
                        connectionProgress = null,
                        error = com.example.amulet.shared.core.AppError.BleError.ConnectionFailed
                    ) }
                }
                .collect { progress ->
                    when (progress) {
                        is DeviceConnectionProgress.Scanning -> {
                            _state.update { it.copy(connectionProgress = "Поиск устройства...") }
                        }
                        is DeviceConnectionProgress.Found -> {
                            _state.update { it.copy(connectionProgress = "Устройство найдено") }
                        }
                        is DeviceConnectionProgress.Connecting -> {
                            _state.update { it.copy(connectionProgress = "Подключение...") }
                        }
                        is DeviceConnectionProgress.Connected -> {
                            connected = true
                            _state.update { it.copy(connectionProgress = "Сохранение...") }
                        }
                        is DeviceConnectionProgress.Failed -> {
                            _state.update { it.copy(
                                isConnecting = false,
                                connectionProgress = null,
                                error = progress.error
                            ) }
                        }
                    }
                }
            
            // Шаг 2: Добавляем в БД
            if (connected) {
                addDeviceUseCase(
                    bleAddress = device.bleAddress,
                    name = deviceName.ifBlank { device.deviceName ?: "Amulet" },
                    hardwareVersion = hardwareVersion
                )
                    .onSuccess { addedDevice ->
                        _state.update { it.copy(
                            isConnecting = false,
                            connectionProgress = null,
                            addedDevice = addedDevice
                        ) }
                        _sideEffects.emit(PairingSideEffect.DeviceAdded(addedDevice))
                    }
                    .onFailure { error ->
                        _state.update { it.copy(
                            isConnecting = false,
                            connectionProgress = null,
                            error = error
                        ) }
                    }
            }
        }
    }
    
    private fun cancelConnection() {
        connectionJob?.cancel()
        connectionJob = null
        _state.update { it.copy(
            isConnecting = false,
            connectionProgress = null,
            selectedDevice = null
        ) }
    }
    
    private fun dismissError() {
        _state.update { it.copy(error = null) }
    }
    
    private fun navigateBack() {
        viewModelScope.launch {
            _sideEffects.emit(PairingSideEffect.NavigateBack)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopScanning()
        connectionJob?.cancel()
    }
}
