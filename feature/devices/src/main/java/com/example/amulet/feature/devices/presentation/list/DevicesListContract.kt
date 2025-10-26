package com.example.amulet.feature.devices.presentation.list

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.devices.model.BleConnectionState
import com.example.amulet.shared.domain.devices.model.Device

/**
 * Контракт для экрана списка устройств.
 */

data class DevicesListState(
    val devices: List<Device> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isEmpty: Boolean = false,
    val connectionStatus: BleConnectionState = BleConnectionState.Disconnected,
    val error: AppError? = null
)

sealed interface DevicesListEvent {
    data object Refresh : DevicesListEvent
    data class DeviceClicked(val deviceId: String) : DevicesListEvent
    data object AddDeviceClicked : DevicesListEvent
    data object DismissError : DevicesListEvent
}

sealed interface DevicesListSideEffect {
    data class NavigateToDeviceDetails(val deviceId: String) : DevicesListSideEffect
    data object NavigateToPairing : DevicesListSideEffect
}
