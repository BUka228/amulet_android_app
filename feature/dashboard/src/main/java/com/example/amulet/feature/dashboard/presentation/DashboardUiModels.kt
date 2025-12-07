package com.example.amulet.feature.dashboard.presentation

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.devices.model.Device

/**
 * UI State для Dashboard экрана.
 * Иммутабельное представление состояния UI.
 */
data class DashboardUiState(
    val isLoading: Boolean = false,
    val userName: String? = null,
    val devices: List<Device> = emptyList(),
    val connectedDevice: Device? = null,
    val connectedBatteryLevel: Int? = null,
    val dailyStats: DailyStats = DailyStats(),
    val quickStartPracticeId: String? = null,
    val quickStartPracticeTitle: String? = null,
    val quickStartPracticeSubtitle: String? = null,
    val recommendedPracticeId: String? = null,
    val error: AppError? = null
)

/**
 * Статистика дня пользователя
 */
data class DailyStats(
    val practiceMinutes: Int = 0,
    val hugsCount: Int = 0,
    val calmLevel: Int = 0 // 0-100
)

/**
 * UI Events - входящие действия от пользователя
 */
sealed interface DashboardUiEvent {
    data class StartPractice(val practiceId: String) : DashboardUiEvent
    data class DeviceClicked(val deviceId: String) : DashboardUiEvent
    data object NavigateToDevicesList : DashboardUiEvent
    data object NavigateToPairing : DashboardUiEvent
    data object NavigateToLibrary : DashboardUiEvent
    data object NavigateToHugs : DashboardUiEvent
    data object NavigateToPatterns : DashboardUiEvent
    data object NavigateToSettings : DashboardUiEvent
    data object ErrorConsumed : DashboardUiEvent
}

/**
 * Side Effects - одноразовые события (навигация, toast)
 */
sealed interface DashboardSideEffect {
    data class NavigateToDeviceDetails(val deviceId: String) : DashboardSideEffect
    data object NavigateToDevicesList : DashboardSideEffect
    data object NavigateToPairing : DashboardSideEffect
    data object NavigateToLibrary : DashboardSideEffect
    data object NavigateToHugs : DashboardSideEffect
    data object NavigateToPatterns : DashboardSideEffect
    data object NavigateToSettings : DashboardSideEffect
    data class ShowToast(val message: String) : DashboardSideEffect
    data class StartPracticeSession(val practiceId: String) : DashboardSideEffect
}
