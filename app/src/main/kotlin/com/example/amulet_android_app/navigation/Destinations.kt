package com.example.amulet_android_app.navigation

import androidx.navigation.NavController
import com.example.amulet.feature.auth.navigation.AuthDestination
import com.example.amulet.feature.auth.navigation.AuthGraph
import com.example.amulet.feature.dashboard.navigation.DashboardGraph
import com.example.amulet.feature.devices.navigation.DevicesGraph
import com.example.amulet.feature.devices.navigation.navigateToPairing as navigateToDevicesPairing
import com.example.amulet.feature.patterns.navigation.PatternsGraph
import com.example.amulet.feature.patterns.navigation.navigateToPatternsList

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

object DevicesGraphDestination : AppDestination {
    override val baseRoute: String = DevicesGraph.route
}

object PatternsGraphDestination : AppDestination {
    override val baseRoute: String = PatternsGraph.route
}

// TODO: Добавить destinations по мере реализации feature модулей
// object LibraryGraphDestination : AppDestination { override val baseRoute: String = LibraryGraph.route }
// object HugsGraphDestination : AppDestination { override val baseRoute: String = HugsGraph.route }
// object SettingsGraphDestination : AppDestination { override val baseRoute: String = SettingsGraph.route }

// Navigation Extensions
fun NavController.navigateToPairing() {
    navigateToDevicesPairing()
}

fun NavController.navigateToDeviceDetails(deviceId: String) {
    navigate("devices/details/$deviceId") {
        launchSingleTop = true
    }
}

fun NavController.navigateToDevicesList() {
    navigate("devices/list") {
        launchSingleTop = true
    }
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
    navigateToPatternsList()
}

fun NavController.navigateToSettings() {
    navigate("settings/main") {
        launchSingleTop = true
    }
}

