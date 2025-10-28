package com.example.amulet_android_app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.amulet_android_app.R
import com.example.amulet.core.design.components.navigation.AmuletBottomNavigationBar
import com.example.amulet.core.design.components.navigation.BottomNavItem
import com.example.amulet.core.design.scaffold.LocalScaffoldState

/**
 * Main Scaffold с упрощенным управлением через ScaffoldState.
 * 
 * Использует глобальный LocalScaffoldState, предоставленный на уровне AmuletApp.
 * 
 * Управление:
 * - Bottom bar устанавливается автоматически для основных экранов
 * - Top bar, FAB и другие элементы управляются напрямую из экранов через scaffoldState.updateConfig
 * - Цвета берутся из темы Material 3 автоматически
 * - При переходах навигационный граф обнуляет конфиг (scaffoldState.reset())
 *
 * @param navController NavHostController для навигации
 * @param modifier Modifier для scaffold
 * @param content Контент приложения (обычно NavHost)
 */
@Composable
fun MainScaffold(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scaffoldState = LocalScaffoldState.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Управляем только bottom bar для основных экранов
    // Все остальное (topBar, FAB) управляется напрямую из экранов
    LaunchedEffect(currentRoute) {
        val showBottomBar = currentRoute != null && shouldShowBottomBar(currentRoute)
        
        scaffoldState.updateConfig {
            copy(
                bottomBar = if (showBottomBar) {
                    {
                        AppBottomNavigationBar(
                            navController = navController,
                            currentRoute = currentRoute.orEmpty()
                        )
                    }
                } else {
                    {}
                }
            )
        }
    }

    val config = scaffoldState.config

    // Scaffold без цветов - берет автоматически из Material 3 темы
    Scaffold(
        modifier = modifier,
        topBar = config.topBar,
        bottomBar = config.bottomBar,
        snackbarHost = config.snackbarHost,
        floatingActionButton = config.floatingActionButton,
        floatingActionButtonPosition = config.floatingActionButtonPosition,
        contentWindowInsets = config.contentWindowInsets
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            content()
        }
    }
}

/**
 * Bottom Navigation Bar приложения.
 * Показывается на главных экранах (dashboard, library, hugs, patterns, settings).
 */
@Composable
private fun AppBottomNavigationBar(
    navController: NavHostController,
    currentRoute: String
) {
    AmuletBottomNavigationBar(
        items = getBottomNavItems(),
        selectedRoute = currentRoute,
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

/**
 * Проверяет, нужно ли показывать bottom bar для данного route.
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
