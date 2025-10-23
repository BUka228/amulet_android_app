package com.example.amulet.feature.dashboard.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.amulet.feature.dashboard.DashboardRoute

object DashboardGraph {
    const val route: String = "dashboard_graph"
}

object DashboardDestination {
    const val main: String = "dashboard/main"
}

fun NavController.navigateToDashboard(popUpToInclusive: Boolean = false) {
    navigate(DashboardDestination.main) {
        if (popUpToInclusive) {
            popUpTo(DashboardDestination.main) { inclusive = true }
        }
        launchSingleTop = true
    }
}

fun NavGraphBuilder.dashboardGraph(
    onNavigateToDeviceDetails: (String) -> Unit,
    onNavigateToDevicesList: () -> Unit,
    onNavigateToPairing: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToHugs: () -> Unit,
    onNavigateToPatterns: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    navigation(startDestination = DashboardDestination.main, route = DashboardGraph.route) {
        composable(route = DashboardDestination.main) {
            DashboardRoute(
                onNavigateToDeviceDetails = onNavigateToDeviceDetails,
                onNavigateToDevicesList = onNavigateToDevicesList,
                onNavigateToPairing = onNavigateToPairing,
                onNavigateToLibrary = onNavigateToLibrary,
                onNavigateToHugs = onNavigateToHugs,
                onNavigateToPatterns = onNavigateToPatterns,
                onNavigateToSettings = onNavigateToSettings
            )
        }
    }
}
