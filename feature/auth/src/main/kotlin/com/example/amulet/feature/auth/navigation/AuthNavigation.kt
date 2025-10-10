package com.example.amulet.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.amulet.feature.auth.presentation.AuthRoute

object AuthGraph {
    const val route: String = "auth"
}

object AuthDestination {
    const val login: String = "auth/login"
}

fun NavController.navigateToAuth(popUpToInclusive: Boolean = false) {
    navigate(AuthDestination.login) {
        if (popUpToInclusive) {
            popUpTo(AuthDestination.login) { inclusive = true }
        }
        launchSingleTop = true
    }
}

fun NavGraphBuilder.authGraph(onAuthSuccess: () -> Unit) {
    navigation(startDestination = AuthDestination.login, route = AuthGraph.route) {
        composable(route = AuthDestination.login) {
            AuthRoute(onAuthSuccess = onAuthSuccess)
        }
    }
}
