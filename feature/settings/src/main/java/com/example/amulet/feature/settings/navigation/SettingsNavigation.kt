package com.example.amulet.feature.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.amulet.feature.settings.presentation.main.SettingsRoute
import com.example.amulet.feature.settings.presentation.profile.ProfileSettingsRoute
import com.example.amulet.feature.settings.presentation.privacy.PrivacySettingsRoute

object SettingsGraph {
    const val route: String = "settings_graph"
}

object SettingsDestination {
    const val main: String = "settings/main"
    const val profile: String = "settings/profile"
    const val privacy: String = "settings/privacy"
}

fun NavGraphBuilder.settingsGraph(
    navController: NavController,
    onSignedOut: () -> Unit,
    onChangePassword: () -> Unit,
) {
    navigation(startDestination = SettingsDestination.main, route = SettingsGraph.route) {
        composable(route = SettingsDestination.main) {
            SettingsRoute(
                onOpenProfile = { navController.navigate(SettingsDestination.profile) },
                onOpenDevices = { navController.navigate("devices/list") },
                onOpenHugsSettings = { navController.navigate("hugs/settings") },
                onOpenPrivacy = { navController.navigate(SettingsDestination.privacy) },
                onSignedOut = onSignedOut,
            )
        }

        composable(route = SettingsDestination.profile) {
            ProfileSettingsRoute(
                onNavigateBack = { navController.popBackStack() },
                onChangePassword = onChangePassword,
            )
        }

        composable(route = SettingsDestination.privacy) {
            PrivacySettingsRoute(
                onNavigateBack = { navController.popBackStack() },
                onAccountDeleted = onSignedOut,
            )
        }
    }
}
