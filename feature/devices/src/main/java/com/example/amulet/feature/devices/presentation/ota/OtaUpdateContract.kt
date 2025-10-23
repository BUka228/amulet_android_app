package com.example.amulet.feature.devices.presentation.ota

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.devices.model.FirmwareUpdate
import com.example.amulet.shared.domain.devices.model.OtaUpdateProgress

/**
 * Контракт для экрана OTA обновления.
 */

data class OtaUpdateState(
    val firmwareUpdate: FirmwareUpdate? = null,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val updateMethod: OtaUpdateMethod? = null,
    val otaProgress: OtaUpdateProgress? = null,
    val error: AppError? = null
)

enum class OtaUpdateMethod {
    BLE,
    WIFI
}

sealed interface OtaUpdateEvent {
    data object StartBleUpdate : OtaUpdateEvent
    data class StartWifiUpdate(val ssid: String, val password: String) : OtaUpdateEvent
    data object CancelUpdate : OtaUpdateEvent
    data object NavigateBack : OtaUpdateEvent
    data object DismissError : OtaUpdateEvent
}

sealed interface OtaUpdateSideEffect {
    data object UpdateCompleted : OtaUpdateSideEffect
    data object NavigateBack : OtaUpdateSideEffect
}
