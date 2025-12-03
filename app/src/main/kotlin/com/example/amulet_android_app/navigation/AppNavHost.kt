package com.example.amulet_android_app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.amulet.feature.auth.navigation.AuthGraph
import com.example.amulet.feature.auth.navigation.authGraph
import com.example.amulet.feature.auth.navigation.navigateToAuth
import com.example.amulet.feature.dashboard.navigation.dashboardGraph
import com.example.amulet.feature.devices.navigation.devicesGraph
import com.example.amulet.feature.patterns.navigation.patternsGraph
import com.example.amulet.feature.patterns.navigation.navigateToPatternEditor
import com.example.amulet.feature.practices.navigation.practicesGraph
import com.example.amulet.feature.practices.navigation.navigateToPracticesHome
import com.example.amulet.feature.practices.navigation.navigateToPracticeSession
import com.example.amulet.feature.hugs.navigation.hugsGraph
import com.example.amulet.feature.settings.navigation.settingsGraph

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
            // Из быстрого доступа на дашборде сразу открываем редактор паттерна (создание нового)
            onNavigateToPatterns = { navController.navigateToPatternEditor() },
            onNavigateToPracticeSession = { practiceId -> navController.navigateToPracticeSession(practiceId) },
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

        // Placeholder destination для раздела «Библиотека»
        composable("library/main") {
            LibraryPlaceholderScreen()
        }

        // Hugs Graph - социальные объятия
        hugsGraph(
            navController = navController
        )

        // Settings Graph - общие настройки приложения и аккаунта
        settingsGraph(
            navController = navController,
            onSignedOut = {
                navController.navigate(AuthGraphDestination.baseRoute) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onChangePassword = {
                navController.navigateToAuth()
            },
        )

        // Auth Graph - экраны входа/регистрации
        authGraph(onAuthSuccess = {
            navController.navigate(DashboardGraphDestination.baseRoute) {
                popUpTo(AuthGraph.route) { inclusive = true }
                launchSingleTop = true
            }
        })
    }
}
