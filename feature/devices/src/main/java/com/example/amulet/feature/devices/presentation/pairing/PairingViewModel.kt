package com.example.amulet.feature.devices.presentation.pairing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.core.logging.Logger
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для экрана добавления устройства.
 * Поддерживает непрерывное BLE сканирование в реальном времени.
 * Сканирование автоматически запускается при открытии экрана и обновляет список устройств динамически.
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
            error = null
        ) }
        
        scanJob = viewModelScope.launch {
            // Сканирование с таймаутом 30 секунд
            // Устройства обновляются в реальном времени по мере обнаружения
            scanForDevicesUseCase(timeoutMs = 30_000L)
                .catch { error ->
                    _state.update { it.copy(
                        isScanning = false,
                        error = com.example.amulet.shared.core.AppError.BleError.DeviceNotFound
                    ) }
                }
                .collect { devicesList ->
                    // Динамическое обновление списка найденных устройств
                    _state.update { it.copy(foundDevices = devicesList) }
                }
            
            // После завершения flow (timeout или ошибка) - останавливаем сканирование
            _state.update { it.copy(isScanning = false) }
        }
    }
    
    private fun stopScanning() {
        scanJob?.cancel()
        scanJob = null
        _state.update { it.copy(isScanning = false) }
    }
    
    private fun selectDevice(device: com.example.amulet.shared.domain.devices.model.ScannedAmulet) {
        stopScanning()
        _state.update { it.copy(selectedDevice = device) }
    }
    
    private fun connectAndAddDevice(deviceName: String) {
        val device = _state.value.selectedDevice ?: return
        
        Logger.d("connectAndAddDevice: start for ${'$'}{device.bleAddress}", tag = TAG)
        _state.update { it.copy(
            isConnecting = true,
            connectionProgress = "Подключение...",
            error = null
        ) }
        
        connectionJob?.cancel()
        connectionJob = viewModelScope.launch {
            // Шаг 1: Подключаемся по BLE и сразу реагируем на Connected/Failed
            val hardwareVersion = 100 // Получим из DeviceStatus после подключения
            Logger.d("connectAndAddDevice: launching connection for ${'$'}{device.bleAddress}", tag = TAG)
            
            val connectionFlow = connectToDeviceUseCase(device.bleAddress)
            val firstState = try {
                connectionFlow.first { state ->
                    when (state) {
                        is com.example.amulet.shared.domain.devices.model.BleConnectionState.Connecting -> {
                            Logger.d("connectAndAddDevice: state=Connecting", tag = TAG)
                            _state.update { it.copy(connectionProgress = "Подключение...") }
                            false
                        }
                        is com.example.amulet.shared.domain.devices.model.BleConnectionState.Connected -> true
                        is com.example.amulet.shared.domain.devices.model.BleConnectionState.Failed -> true
                        else -> false
                    }
                }
            } catch (e: Throwable) {
                Logger.e("connectAndAddDevice: exception during connection flow: ${'$'}e", tag = TAG)
                _state.update { it.copy(
                    isConnecting = false,
                    connectionProgress = null,
                    error = com.example.amulet.shared.core.AppError.BleError.ConnectionFailed
                ) }
                return@launch
            }
            
            Logger.d("connectAndAddDevice: firstState=${'$'}firstState", tag = TAG)
            when (firstState) {
                is com.example.amulet.shared.domain.devices.model.BleConnectionState.Connected -> {
                    // Шаг 2: Добавляем в БД сразу после успешного подключения
                    _state.update { it.copy(connectionProgress = "Сохранение...") }
                    Logger.d("connectAndAddDevice: adding device to DB", tag = TAG)
                    addDeviceUseCase(
                        bleAddress = device.bleAddress,
                        name = deviceName.ifBlank { device.deviceName },
                        hardwareVersion = hardwareVersion
                    )
                        .onSuccess { addedDevice ->
                            Logger.d("connectAndAddDevice: device added successfully id=${'$'}{addedDevice.id.value}", tag = TAG)
                            _state.update { it.copy(
                                isConnecting = false,
                                connectionProgress = null,
                                addedDevice = addedDevice
                            ) }
                            _sideEffects.emit(PairingSideEffect.DeviceAdded(addedDevice))
                        }
                        .onFailure { error ->
                            Logger.e("connectAndAddDevice: addDeviceUseCase failed: ${'$'}error", tag = TAG)
                            _state.update { it.copy(
                                isConnecting = false,
                                connectionProgress = null,
                                error = error
                            ) }
                        }
                }
                is com.example.amulet.shared.domain.devices.model.BleConnectionState.Failed -> {
                    Logger.e("connectAndAddDevice: connection failed: ${'$'}{firstState.error}", tag = TAG)
                    _state.update { it.copy(
                        isConnecting = false,
                        connectionProgress = null,
                        error = firstState.error
                    ) }
                }
                else -> { /* Игнорируем другие состояния */ }
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
    
    companion object {
        private const val TAG = "PairingViewModel"
    }
}
