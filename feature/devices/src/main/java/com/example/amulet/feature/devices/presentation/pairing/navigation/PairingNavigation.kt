package com.example.amulet.feature.devices.presentation.pairing.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.amulet.feature.devices.presentation.pairing.screens.PairingChooseMethodScreen
import com.example.amulet.feature.devices.presentation.pairing.screens.PairingConfirmScreen
import com.example.amulet.feature.devices.presentation.pairing.screens.PairingProgressScreen
import com.example.amulet.feature.devices.presentation.pairing.screens.PairingQrScanScreen
import com.example.amulet.feature.devices.presentation.pairing.screens.PairingNfcReadScreen
import com.example.amulet.feature.devices.presentation.pairing.screens.PairingSuccessScreen

/**
 * Граф навигации для процесса паринга устройства
 */
object PairingGraph {
    const val route = "pairing_graph"
}

object PairingDestination {
    const val chooseMethod = "pairing/choose_method"
    const val qrScan = "pairing/qr_scan"
    const val nfcRead = "pairing/nfc_read"
    const val confirm = "pairing/confirm"
    const val progress = "pairing/progress"
    const val success = "pairing/success"
}

fun NavController.navigateToPairingChooseMethod() {
    navigate(PairingDestination.chooseMethod) {
        popUpTo(PairingGraph.route) { inclusive = false }
        launchSingleTop = true
    }
}

fun NavController.navigateToPairingQrScan() {
    navigate(PairingDestination.qrScan)
}

fun NavController.navigateToPairingNfcRead() {
    navigate(PairingDestination.nfcRead)
}

fun NavController.navigateToPairingConfirm() {
    navigate(PairingDestination.confirm) {
        popUpTo(PairingDestination.chooseMethod) { inclusive = false }
    }
}

fun NavController.navigateToPairingProgress() {
    navigate(PairingDestination.progress) {
        popUpTo(PairingDestination.confirm) { inclusive = true }
    }
}

fun NavController.navigateToPairingSuccess() {
    navigate(PairingDestination.success) {
        popUpTo(PairingGraph.route) { inclusive = false }
        launchSingleTop = true
    }
}

fun NavGraphBuilder.pairingGraph(
    navController: NavController,
    onPairingComplete: () -> Unit,
    onNavigateBack: () -> Unit
) {
    navigation(
        startDestination = PairingDestination.chooseMethod,
        route = PairingGraph.route
    ) {
        composable(route = PairingDestination.chooseMethod) {
            PairingChooseMethodScreen(
                onQrScanSelected = { navController.navigateToPairingQrScan() },
                onNfcSelected = { navController.navigateToPairingNfcRead() },
                onNavigateBack = onNavigateBack
            )
        }

        composable(route = PairingDestination.qrScan) {
            PairingQrScanScreen(
                onQrScanned = { navController.navigateToPairingConfirm() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = PairingDestination.nfcRead) {
            PairingNfcReadScreen(
                onNfcRead = { navController.navigateToPairingConfirm() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = PairingDestination.confirm) {
            PairingConfirmScreen(
                onConfirm = { navController.navigateToPairingProgress() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = PairingDestination.progress) {
            PairingProgressScreen(
                onSuccess = { navController.navigateToPairingSuccess() },
                onError = { navController.navigateToPairingChooseMethod() }
            )
        }

        composable(route = PairingDestination.success) {
            PairingSuccessScreen(
                onComplete = onPairingComplete
            )
        }
    }
}
