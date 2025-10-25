package com.example.amulet.feature.devices.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.amulet.feature.devices.presentation.details.DeviceDetailsRoute
import com.example.amulet.feature.devices.presentation.list.DevicesListRoute
import com.example.amulet.feature.devices.presentation.ota.OtaUpdateRoute
import com.example.amulet.feature.devices.presentation.pairing.navigation.PairingGraph
import com.example.amulet.feature.devices.presentation.pairing.navigation.pairingGraph

object DevicesGraph {
    const val route: String = "devices_graph"
}

object DevicesDestination {
    const val list: String = "devices/list"
    const val details: String = "devices/details/{deviceId}"
    const val otaUpdate: String = "devices/ota/{deviceId}"
}

fun NavController.navigateToDevicesList(popUpToInclusive: Boolean = false) {
    navigate(DevicesDestination.list) {
        if (popUpToInclusive) {
            popUpTo(DevicesDestination.list) { inclusive = true }
        }
        launchSingleTop = true
    }
}

fun NavController.navigateToPairing() {
    navigate(PairingGraph.route) {
        launchSingleTop = true
    }
}

fun NavController.navigateToDeviceDetails(deviceId: String) {
    navigate("devices/details/$deviceId")
}

fun NavController.navigateToOtaUpdate(deviceId: String) {
    navigate("devices/ota/$deviceId")
}

fun NavGraphBuilder.devicesGraph(
    navController: NavController,
    onNavigateBack: () -> Unit
) {
    navigation(startDestination = DevicesDestination.list, route = DevicesGraph.route) {
        composable(route = DevicesDestination.list) {
            DevicesListRoute(
                onNavigateToPairing = { navController.navigateToPairing() },
                onNavigateToDeviceDetails = { deviceId -> navController.navigateToDeviceDetails(deviceId) },
                onNavigateBack = onNavigateBack
            )
        }

        // Вложенный граф паринга
        pairingGraph(
            navController = navController,
            onPairingComplete = { 
                navController.popBackStack(DevicesDestination.list, inclusive = false)
            },
            onNavigateBack = { 
                navController.popBackStack()
            }
        )

        composable(
            route = DevicesDestination.details,
            arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: return@composable
            DeviceDetailsRoute(
                deviceId = deviceId,
                onNavigateToOta = { navController.navigateToOtaUpdate(deviceId) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = DevicesDestination.otaUpdate,
            arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: return@composable
            OtaUpdateRoute(
                deviceId = deviceId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
