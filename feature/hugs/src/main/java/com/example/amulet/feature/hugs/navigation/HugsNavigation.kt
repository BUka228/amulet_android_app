package com.example.amulet.feature.hugs.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.example.amulet.feature.hugs.presentation.details.HugDetailsRoute
import com.example.amulet.feature.hugs.presentation.emotions.HugsEmotionsRoute
import com.example.amulet.feature.hugs.presentation.history.HugsHistoryRoute
import com.example.amulet.feature.hugs.presentation.main.HugsRoute
import com.example.amulet.feature.hugs.presentation.pairing.HugsPairingRoute
import com.example.amulet.feature.hugs.presentation.secretcodes.HugsSecretCodesRoute
import com.example.amulet.feature.hugs.presentation.settings.HugsSettingsRoute
import com.example.amulet.feature.patterns.navigation.navigateToPatternEditor

object HugsGraph {
    const val route: String = "hugs_graph"
}

object HugsDestination {
    const val main: String = "hugs/main"
    const val history: String = "hugs/history"
    const val settings: String = "hugs/settings"
    const val emotions: String = "hugs/emotions"
    const val secretCodes: String = "hugs/secret_codes"
    const val pairing: String = "hugs/pairing"
    const val details: String = "hugs/details/{hugId}"
}

fun NavController.navigateToHugs(popUpToInclusive: Boolean = false) {
    navigate(HugsDestination.main) {
        if (popUpToInclusive) {
            popUpTo(HugsDestination.main) { inclusive = true }
        }
        launchSingleTop = true
    }
}

fun NavController.navigateToHugsHistory() {
    navigate(HugsDestination.history) {
        launchSingleTop = true
    }
}

fun NavController.navigateToHugsSettings() {
    navigate(HugsDestination.settings) {
        launchSingleTop = true
    }
}

fun NavController.navigateToHugsEmotions() {
    navigate(HugsDestination.emotions) {
        launchSingleTop = true
    }
}

fun NavController.navigateToHugsSecretCodes() {
    navigate(HugsDestination.secretCodes) {
        launchSingleTop = true
    }
}

fun NavController.navigateToHugsPairing() {
    navigate(HugsDestination.pairing) {
        launchSingleTop = true
    }
}

fun NavController.navigateToHugDetails(hugId: String) {
    navigate("hugs/details/$hugId") {
        launchSingleTop = true
    }
}

fun NavGraphBuilder.hugsGraph(
    navController: NavController,
) {
    navigation(startDestination = HugsDestination.main, route = HugsGraph.route) {
        composable(
            route = HugsDestination.main,
            deepLinks = listOf(
                navDeepLink { uriPattern = "amulet://hugs" },
                navDeepLink { uriPattern = "https://amulet.app/hugs" }
            )
        ) {
            HugsRoute(
                onOpenHistory = { navController.navigateToHugsHistory() },
                onOpenSettings = { navController.navigateToHugsSettings() },
                onOpenEmotions = { navController.navigateToHugsEmotions() },
                onOpenSecretCodes = { navController.navigateToHugsSecretCodes() },
                onOpenPairing = { navController.navigateToHugsPairing() },
            )
        }

        composable(route = HugsDestination.history) {
            HugsHistoryRoute(
                onNavigateBack = { navController.popBackStack() },
                onOpenDetails = { hugId -> navController.navigateToHugDetails(hugId) },
            )
        }

        composable(route = HugsDestination.settings) {
            HugsSettingsRoute(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(route = HugsDestination.emotions) {
            HugsEmotionsRoute(
                onNavigateBack = { navController.popBackStack() },
                onOpenPatternEditor = { patternId ->
                    navController.navigateToPatternEditor(patternId)
                }
            )
        }

        composable(route = HugsDestination.secretCodes) {
            HugsSecretCodesRoute(
                onNavigateBack = { navController.popBackStack() },
                onOpenPatternDetails = { patternId ->
                    navController.navigateToPatternEditor(patternId)
                }
            )
        }

        composable(
            route = HugsDestination.pairing + "?code={code}&inviterName={inviterName}",
            arguments = listOf(
                navArgument("code") { type = NavType.StringType; nullable = true },
                navArgument("inviterName") { type = NavType.StringType; nullable = true },
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "amulet://hugs/pair?code={code}&inviterName={inviterName}" },
                navDeepLink { uriPattern = "https://amulet.app/hugs/pair?code={code}&inviterName={inviterName}" },
            )
        ) {
            HugsPairingRoute(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = HugsDestination.details,
            arguments = listOf(
                navArgument("hugId") { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "amulet://hugs/{hugId}" },
                navDeepLink { uriPattern = "https://amulet.app/hugs/{hugId}" }
            )
        ) { backStackEntry ->
            val hugId = backStackEntry.arguments?.getString("hugId") ?: return@composable
            HugDetailsRoute(hugId = hugId)
        }
    }
}
