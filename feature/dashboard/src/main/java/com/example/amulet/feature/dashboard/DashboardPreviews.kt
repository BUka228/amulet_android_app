package com.example.amulet.feature.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.amulet.core.design.foundation.theme.AmuletTheme
import com.example.amulet.feature.dashboard.presentation.*

/**
 * Preview функции для Dashboard экрана.
 * Позволяют быстро проверять UI в Android Studio без запуска приложения.
 */

// ===== Preview: Dashboard с подключенным амулетом =====
@Preview(
    name = "Dashboard - Connected Device",
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun DashboardScreenConnectedPreview() {
    AmuletTheme {
        DashboardScreen(
            uiState = DashboardUiState(
                userName = "Александр",
                deviceStatus = DeviceStatus(
                    name = "Amulet AMU-200",
                    connectionStatus = "connected",
                    batteryLevel = 85,
                    currentAnimation = "Pulse"
                ),
                dailyStats = DailyStats(
                    practiceMinutes = 42,
                    hugsCount = 5,
                    calmLevel = 75
                )
            ),
            onEvent = {}
        )
    }
}

// ===== Preview: Dashboard без устройства =====
@Preview(
    name = "Dashboard - No Device",
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun DashboardScreenNoDevicePreview() {
    AmuletTheme {
        DashboardScreen(
            uiState = DashboardUiState(
                userName = "Мария",
                deviceStatus = null,
                dailyStats = DailyStats(
                    practiceMinutes = 0,
                    hugsCount = 0,
                    calmLevel = 0
                )
            ),
            onEvent = {}
        )
    }
}

// ===== Preview: Dashboard с низкой батареей =====
@Preview(
    name = "Dashboard - Low Battery",
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun DashboardScreenLowBatteryPreview() {
    AmuletTheme {
        DashboardScreen(
            uiState = DashboardUiState(
                userName = "Дмитрий",
                deviceStatus = DeviceStatus(
                    name = "Amulet AMU-100",
                    connectionStatus = "connected",
                    batteryLevel = 15,
                    currentAnimation = null
                ),
                dailyStats = DailyStats(
                    practiceMinutes = 120,
                    hugsCount = 12,
                    calmLevel = 92
                )
            ),
            onEvent = {}
        )
    }
}

// ===== Preview: Dark Theme =====
@Preview(
    name = "Dashboard - Dark Theme",
    showBackground = true,
    showSystemUi = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun DashboardScreenDarkPreview() {
    AmuletTheme(darkTheme = true) {
        DashboardScreen(
            uiState = DashboardUiState(
                userName = "Елена",
                deviceStatus = DeviceStatus(
                    name = "Amulet AMU-200",
                    connectionStatus = "connected",
                    batteryLevel = 65,
                    currentAnimation = "Breathing"
                ),
                dailyStats = DailyStats(
                    practiceMinutes = 28,
                    hugsCount = 3,
                    calmLevel = 58
                )
            ),
            onEvent = {}
        )
    }
}

// ===== Preview: Отдельные компоненты =====
@Preview(name = "Amulet Status - Connected", showBackground = true)
@Composable
private fun AmuletStatusCardConnectedPreview() {
    AmuletTheme {
        AmuletStatusCard(
            device = DeviceStatus(
                name = "Amulet AMU-200",
                connectionStatus = "connected",
                batteryLevel = 85,
                currentAnimation = "Pulse"
            ),
            onNavigateToPairing = {}
        )
    }
}

@Preview(name = "Amulet Status - No Device", showBackground = true)
@Composable
private fun AmuletStatusCardNoDevicePreview() {
    AmuletTheme {
        AmuletStatusCard(
            device = null,
            onNavigateToPairing = {}
        )
    }
}

@Preview(name = "Quick Start Section", showBackground = true)
@Composable
private fun QuickStartSectionPreview() {
    AmuletTheme {
        QuickStartSection(
            onStartPractice = {}
        )
    }
}

@Preview(name = "Daily Stats Section", showBackground = true)
@Composable
private fun DailyStatsSectionPreview() {
    AmuletTheme {
        DailyStatsSection(
            stats = DailyStats(
                practiceMinutes = 42,
                hugsCount = 5,
                calmLevel = 75
            )
        )
    }
}

@Preview(name = "Quick Access Grid", showBackground = true)
@Composable
private fun QuickAccessGridPreview() {
    AmuletTheme {
        QuickAccessGrid(
            onNavigateToLibrary = {},
            onNavigateToHugs = {},
            onNavigateToPatterns = {}
        )
    }
}
