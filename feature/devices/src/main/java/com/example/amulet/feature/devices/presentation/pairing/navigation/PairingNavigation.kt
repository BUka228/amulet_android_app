package com.example.amulet.feature.devices.presentation.pairing.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.amulet.feature.devices.presentation.pairing.screens.PairingScreen

/**
 * Упрощенная навигация для добавления устройства.
 * Один экран паринга.
 */
object PairingDestination {
    const val pairing = "pairing"
}

fun NavController.navigateToPairing() {
    navigate(PairingDestination.pairing) {
        launchSingleTop = true
    }
}

fun NavGraphBuilder.pairingGraph(
    onNavigateBack: () -> Unit,
    onPairingComplete: () -> Unit
) {
    composable(route = PairingDestination.pairing) {
        PairingScreen(
            onNavigateBack = onNavigateBack,
            onDeviceAdded = onPairingComplete
        )
    }
}
