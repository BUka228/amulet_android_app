package com.example.amulet_android_app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.amulet_android_app.presentation.main.MainScreen
import com.example.amulet.feature.auth.navigation.AuthGraph
import com.example.amulet.feature.auth.navigation.authGraph

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

        authGraph(onAuthSuccess = {
            navController.navigate(MainGraphDestination.baseRoute) {
                popUpTo(AuthGraph.route) { inclusive = true }
                launchSingleTop = true
            }
        })
    }
}
