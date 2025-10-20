package com.example.amulet_android_app.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.amulet.core.design.AmuletTheme
import com.example.amulet_android_app.presentation.session.AuthState
import com.example.amulet_android_app.presentation.session.SessionViewModel
import com.example.amulet_android_app.presentation.splash.SplashScreen
import com.example.amulet_android_app.navigation.AppNavHost
import com.example.amulet_android_app.navigation.AuthGraphDestination
import com.example.amulet_android_app.navigation.DashboardGraphDestination
import com.example.amulet_android_app.navigation.MainScaffold

@Composable
fun AmuletApp(
    modifier: Modifier = Modifier,
    sessionViewModel: SessionViewModel = hiltViewModel()
) {
    val authState by sessionViewModel.state.collectAsStateWithLifecycle()

    AmuletTheme {
        Crossfade(targetState = authState, modifier = modifier, label = "authState") { state ->
            when (state) {
                AuthState.Loading -> SplashScreen()
                AuthState.LoggedOut -> key(AuthState.LoggedOut) {
                    val navController = rememberNavController()
                    AppNavHost(
                        navController = navController,
                        startDestination = AuthGraphDestination,
                    )
                }
                is AuthState.LoggedIn -> key(AuthState.LoggedIn) {
                    val navController = rememberNavController()
                    MainScaffold(navController = navController) { contentModifier ->
                        AppNavHost(
                            navController = navController,
                            startDestination = DashboardGraphDestination,
                            modifier = contentModifier
                        )
                    }
                }
                AuthState.Guest -> key(AuthState.Guest) {
                    val navController = rememberNavController()
                    MainScaffold(navController = navController) { contentModifier ->
                        AppNavHost(
                            navController = navController,
                            startDestination = DashboardGraphDestination,
                            modifier = contentModifier
                        )
                    }
                }
            }
        }
    }
}
