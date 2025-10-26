package com.example.amulet.feature.devices.presentation.pairing

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.devices.model.Device
import com.example.amulet.shared.domain.devices.model.ScannedAmulet

/**
 * Контракт для экрана добавления устройства.
 * Упрощенный флоу: сканирование BLE -> выбор устройства -> подключение -> добавление в БД.
 */

data class PairingState(
    val isScanning: Boolean = false,
    val foundDevices: List<ScannedAmulet> = emptyList(),
    val selectedDevice: ScannedAmulet? = null,
    val isConnecting: Boolean = false,
    val connectionProgress: String? = null,
    val addedDevice: Device? = null,
    val error: AppError? = null,
    val showNfcHint: Boolean = true
)

sealed interface PairingEvent {
    data object StartScanning : PairingEvent
    data object StopScanning : PairingEvent
    data class SelectDevice(val device: ScannedAmulet) : PairingEvent
    data class ConnectAndAddDevice(val deviceName: String) : PairingEvent
    data object CancelConnection : PairingEvent
    data object DismissError : PairingEvent
    data object NavigateBack : PairingEvent
}

sealed interface PairingSideEffect {
    data class DeviceAdded(val device: Device) : PairingSideEffect
    data object NavigateBack : PairingSideEffect
}
