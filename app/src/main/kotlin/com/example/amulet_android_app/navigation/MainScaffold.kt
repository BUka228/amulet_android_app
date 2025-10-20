package com.example.amulet_android_app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.example.amulet_android_app.R
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.amulet.core.design.components.navigation.AmuletBottomNavigationBar
import com.example.amulet.core.design.components.navigation.BottomNavItem

/**
 * Main Scaffold с Bottom Navigation Bar
 * Показывается только для основных экранов приложения (не для Auth)
 */
@Composable
fun MainScaffold(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Определяем, показывать ли bottom bar
    val showBottomBar = currentRoute != null && shouldShowBottomBar(currentRoute)
    
    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                AmuletBottomNavigationBar(
                    items = getBottomNavItems(),
                    selectedRoute = currentRoute.orEmpty(),
                    onItemSelected = { item ->
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                // Очищаем backstack до первого destination в графе
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        content(Modifier.padding(paddingValues))
    }
}

/**
 * Проверяет, нужно ли показывать bottom bar для данного route
 */
private fun shouldShowBottomBar(route: String): Boolean {
    // Bottom bar показывается только на основных экранах
    return route.startsWith("dashboard") ||
           route.startsWith("library") ||
           route.startsWith("hugs") ||
           route.startsWith("patterns") ||
           route.startsWith("settings")
}

/**
 * Получить список элементов bottom navigation
 */
@Composable
private fun getBottomNavItems() = listOf(
    BottomNavItem(
        route = "dashboard/main",
        icon = Icons.Default.Home,
        label = stringResource(R.string.bottom_nav_home)
    ),
    BottomNavItem(
        route = "library/main",
        icon = Icons.AutoMirrored.Filled.List,
        label = stringResource(R.string.bottom_nav_library)
    ),
    BottomNavItem(
        route = "hugs/main",
        icon = Icons.Default.Favorite,
        label = stringResource(R.string.bottom_nav_hugs)
    ),
    BottomNavItem(
        route = "patterns/main",
        icon = Icons.Default.Notifications,
        label = stringResource(R.string.bottom_nav_patterns)
    ),
    BottomNavItem(
        route = "settings/main",
        icon = Icons.Default.Settings,
        label = stringResource(R.string.bottom_nav_settings)
    )
)
