package com.example.amulet_android_app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.amulet.feature.auth.navigation.AuthGraph
import com.example.amulet.feature.auth.navigation.authGraph
import com.example.amulet.feature.dashboard.navigation.dashboardGraph
import com.example.amulet.feature.devices.navigation.devicesGraph
import com.example.amulet.feature.patterns.navigation.patternsGraph
import com.example.amulet.feature.practices.navigation.practicesGraph
import com.example.amulet.feature.practices.navigation.navigateToPracticesHome

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: AppDestination,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination.baseRoute,
        modifier = modifier
    ) {
        // Dashboard Graph - главный экран после авторизации
        dashboardGraph(
            onNavigateToDeviceDetails = navController::navigateToDeviceDetails,
            onNavigateToDevicesList = navController::navigateToDevicesList,
            onNavigateToPairing = navController::navigateToPairing,
            onNavigateToLibrary = { navController.navigateToPracticesHome() },
            onNavigateToHugs = navController::navigateToHugs,
            onNavigateToPatterns = navController::navigateToPatterns,
            onNavigateToSettings = navController::navigateToSettings
        )

        // Devices Graph - управление устройствами, паринг, OTA
        devicesGraph(
            navController = navController,
            onNavigateBack = { navController.popBackStack() }
        )

        // Patterns Graph - управление паттернами световых анимаций
        patternsGraph(
            navController = navController
        )

        // Practices Graph - управление практиками
        practicesGraph(
            navController = navController,
            onNavigateToPairing = navController::navigateToPairing
        )

        // Placeholder destinations для bottom navigation
        composable("library/main") {
            LibraryPlaceholderScreen()
        }
        
        composable("hugs/main") {
            HugsPlaceholderScreen()
        }
        
        composable("settings/main") {
            SettingsPlaceholderScreen()
        }

        // Auth Graph - экраны входа/регистрации
        authGraph(onAuthSuccess = {
            navController.navigate(DashboardGraphDestination.baseRoute) {
                popUpTo(AuthGraph.route) { inclusive = true }
                launchSingleTop = true
            }
        })
    }
}
