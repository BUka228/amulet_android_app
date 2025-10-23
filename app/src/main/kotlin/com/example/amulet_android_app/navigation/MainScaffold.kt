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
import com.example.amulet.core.design.scaffold.ProvideScaffoldState
import com.example.amulet.core.design.scaffold.ScaffoldConfig
import com.example.amulet.core.design.scaffold.rememberScaffoldState

/**
 * Main Scaffold с централизованным управлением через ScaffoldState.
 * Enterprise-паттерн: Single Scaffold with Shared State.
 *
 * Преимущества:
 * - Нет вложенных Scaffold - правильная работа с padding и insets
 * - Гибкое управление topBar/bottomBar/FAB из любого экрана через LocalScaffoldState
 * - Поддержка всех Material 3 анимаций и transitions
 * - Масштабируемая архитектура для сложных UX паттернов
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
    val scaffoldState = rememberScaffoldState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Автоматически управляем scaffold конфигурацией в зависимости от route
    // При смене route сбрасываем всю конфигурацию и устанавливаем только bottomBar если нужно
    LaunchedEffect(currentRoute) {
        val showBottomBar = currentRoute != null && shouldShowBottomBar(currentRoute)
        if (showBottomBar) {
            // Для главных экранов: только bottomBar, без topBar и FAB
            scaffoldState.updateConfig {
                ScaffoldConfig(
                    bottomBar = {
                        AppBottomNavigationBar(
                            navController = navController,
                            currentRoute = currentRoute.orEmpty()
                        )
                    }
                )
            }
        } else {
            // Для вложенных экранов: сбрасываем bottomBar, topBar/FAB установят сами экраны
            scaffoldState.reset()
        }
    }

    val config = scaffoldState.config

    Scaffold(
        modifier = modifier,
        topBar = config.topBar,
        bottomBar = config.bottomBar,
        snackbarHost = config.snackbarHost,
        floatingActionButton = config.floatingActionButton,
        floatingActionButtonPosition = config.floatingActionButtonPosition,
        containerColor = config.containerColor,
        contentColor = config.contentColor,
        contentWindowInsets = config.contentWindowInsets
    ) { paddingValues ->
        // Предоставляем ScaffoldState через CompositionLocal для доступа из любого экрана
        // Оборачиваем контент в Box с padding для корректной работы с topBar/bottomBar/FAB
        ProvideScaffoldState(scaffoldState) {
            Box(modifier = Modifier.padding(paddingValues)) {
                content()
            }
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
