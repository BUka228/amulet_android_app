package com.example.amulet_android_app.navigation

import androidx.navigation.NavController
import com.example.amulet.feature.auth.navigation.AuthDestination
import com.example.amulet.feature.auth.navigation.AuthGraph
import com.example.amulet.feature.dashboard.navigation.DashboardGraph

sealed interface AppDestination {
    val baseRoute: String
}

object AuthGraphDestination : AppDestination {
    override val baseRoute: String = AuthGraph.route
}

object AuthLoginDestination : AppDestination {
    override val baseRoute: String = AuthDestination.login
}

object DashboardGraphDestination : AppDestination {
    override val baseRoute: String = DashboardGraph.route
}

// TODO: Добавить destinations по мере реализации feature модулей
// object PairingGraphDestination : AppDestination { override val baseRoute: String = PairingGraph.route }
// object LibraryGraphDestination : AppDestination { override val baseRoute: String = LibraryGraph.route }
// object HugsGraphDestination : AppDestination { override val baseRoute: String = HugsGraph.route }
// object PatternsGraphDestination : AppDestination { override val baseRoute: String = PatternsGraph.route }
// object SettingsGraphDestination : AppDestination { override val baseRoute: String = SettingsGraph.route }

// Navigation Extensions
fun NavController.navigateToPairing() {
    // TODO: Реализовать когда :feature:pairing будет готов
    println("Навигация к Pairing (не реализовано)")
}

fun NavController.navigateToLibrary() {
    navigate("library/main") {
        launchSingleTop = true
    }
}

fun NavController.navigateToHugs() {
    navigate("hugs/main") {
        launchSingleTop = true
    }
}

fun NavController.navigateToPatterns() {
    navigate("patterns/main") {
        launchSingleTop = true
    }
}

fun NavController.navigateToSettings() {
    navigate("settings/main") {
        launchSingleTop = true
    }
}

