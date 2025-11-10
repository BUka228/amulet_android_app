package com.example.amulet.feature.patterns.presentation.preview

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.devices.model.Device
import com.example.amulet.shared.domain.patterns.model.Pattern
import com.example.amulet.shared.domain.patterns.model.PatternSpec
import com.example.amulet.shared.domain.patterns.usecase.PreviewProgress

/**
 * Контракт для экрана предпросмотра паттерна.
 */

data class PatternPreviewState(
    val pattern: Pattern? = null,
    val spec: PatternSpec? = null,
    val devices: List<Device> = emptyList(),
    val selectedDevice: Device? = null,
    val isLoading: Boolean = true,
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val progress: PreviewProgress? = null,
    val localPreview: Boolean = true, // Локальная визуализация или на устройстве
    val error: AppError? = null
)

sealed interface PatternPreviewEvent {
    data object LoadDevices : PatternPreviewEvent
    data class SelectDevice(val deviceId: String) : PatternPreviewEvent
    data object TogglePreviewMode : PatternPreviewEvent
    data object PlayPattern : PatternPreviewEvent
    data object PausePattern : PatternPreviewEvent
    data object StopPattern : PatternPreviewEvent
    data object SendToDevice : PatternPreviewEvent
    data object DismissError : PatternPreviewEvent
}

sealed interface PatternPreviewSideEffect {
    data class ShowSnackbar(val message: String) : PatternPreviewSideEffect
    data object ShowDeviceRequired : PatternPreviewSideEffect
    data class ShowBleConnectionError(val error: AppError) : PatternPreviewSideEffect
    data object NavigateToDeviceSelection : PatternPreviewSideEffect
}
