package com.example.amulet_android_app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.amulet_android_app.presentation.auth.AuthHost
import com.example.amulet_android_app.presentation.main.MainScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: AppDestination,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination.baseRoute,
        modifier = modifier
    ) {
        navigation(startDestination = DashboardDestination.baseRoute, route = MainGraphDestination.baseRoute) {
            composable(DashboardDestination.baseRoute) {
                MainScreen(
                    onOpenSettings = { navController.navigateToSettings() }
                )
            }
            composable(SettingsDestination.baseRoute) {
                MainScreen(
                    onOpenSettings = { /* already in settings */ }
                )
            }
        }

        navigation(startDestination = AuthLoginDestination.baseRoute, route = AuthGraphDestination.baseRoute) {
            composable(AuthLoginDestination.baseRoute) {
                AuthHost(onAuthSuccess = {
                    navController.navigate(MainGraphDestination.baseRoute) {
                        popUpTo(AuthGraphDestination.baseRoute) { inclusive = true }
                        launchSingleTop = true
                    }
                })
            }
        }
    }
}
