package com.example.amulet_android_app.navigation

import androidx.navigation.NavController

sealed interface AppDestination {
    val baseRoute: String
}

object MainGraphDestination : AppDestination {
    override val baseRoute: String = "main"
}

object AuthGraphDestination : AppDestination {
    override val baseRoute: String = "auth"
}

object AuthLoginDestination : AppDestination {
    override val baseRoute: String = "auth/login"
}

object ProfileDestination : AppDestination {
    override val baseRoute: String = "profile"
}

object DashboardDestination : AppDestination {
    override val baseRoute: String = "dashboard"
}

object SettingsDestination : AppDestination {
    override val baseRoute: String = "settings"
}

fun NavController.navigateToDashboard() {
    navigate(DashboardDestination.baseRoute) {
        launchSingleTop = true
    }
}

fun NavController.navigateToSettings() {
    navigate(SettingsDestination.baseRoute)
}

