package com.example.amulet_android_app.navigation

import androidx.navigation.NavController
import com.example.amulet.feature.auth.navigation.AuthDestination
import com.example.amulet.feature.auth.navigation.AuthGraph

sealed interface AppDestination {
    val baseRoute: String
}

object MainGraphDestination : AppDestination {
    override val baseRoute: String = "main"
}

object AuthGraphDestination : AppDestination {
    override val baseRoute: String = AuthGraph.route
}

object AuthLoginDestination : AppDestination {
    override val baseRoute: String = AuthDestination.login
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

