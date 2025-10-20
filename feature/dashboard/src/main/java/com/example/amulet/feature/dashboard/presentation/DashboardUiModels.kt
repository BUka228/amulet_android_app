package com.example.amulet.feature.dashboard.presentation

import com.example.amulet.shared.core.AppError

/**
 * UI State для Dashboard экрана.
 * Иммутабельное представление состояния UI.
 */
data class DashboardUiState(
    val isLoading: Boolean = false,
    val userName: String = "Пользователь",
    val deviceStatus: DeviceStatus? = null,
    val dailyStats: DailyStats = DailyStats(),
    val error: AppError? = null
)

/**
 * Состояние подключенного устройства
 */
data class DeviceStatus(
    val name: String,
    val connectionStatus: String, // connected, connecting, disconnected
    val batteryLevel: Int,
    val currentAnimation: String?
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
    data object Refresh : DashboardUiEvent
    data class StartPractice(val practiceId: String) : DashboardUiEvent
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
    data object NavigateToPairing : DashboardSideEffect
    data object NavigateToLibrary : DashboardSideEffect
    data object NavigateToHugs : DashboardSideEffect
    data object NavigateToPatterns : DashboardSideEffect
    data object NavigateToSettings : DashboardSideEffect
    data class ShowToast(val message: String) : DashboardSideEffect
    data class StartPracticeSession(val practiceId: String) : DashboardSideEffect
}
