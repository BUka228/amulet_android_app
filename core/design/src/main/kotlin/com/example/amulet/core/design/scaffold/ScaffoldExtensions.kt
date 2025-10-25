package com.example.amulet.core.design.scaffold

import androidx.compose.material3.FabPosition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * Extension functions для упрощённой работы со ScaffoldState.
 * Enterprise-паттерн: Declarative API для управления UI state.
 */

/**
 * Декларативно устанавливает top bar при входе на экран.
 * MainScaffold автоматически очистит конфигурацию при смене route.
 *
 * Использование:
 * ```kotlin
 * scaffoldState.SetupTopBar {
 *     TopAppBar(
 *         title = { Text("My Screen") },
 *         navigationIcon = { BackButton() }
 *     )
 * }
 * ```
 *
 * @param topBar Composable функция для top app bar
 */
@Composable
fun ScaffoldState.SetupTopBar(topBar: @Composable () -> Unit) {
    LaunchedEffect(Unit) {
        updateTopBar(topBar)
    }
}

/**
 * Устанавливает top bar с ключом для отслеживания изменений.
 * Используется когда нужно обновить topBar при изменении state или при навигации с restoreState.
 *
 * Использование:
 * ```kotlin
 * scaffoldState.ConfigureTopBar(uiState.userName) {
 *     TopAppBar(
 *         title = { Text(uiState.userName) },
 *         navigationIcon = { BackButton() }
 *     )
 * }
 * ```
 *
 * @param key Ключи для отслеживания изменений (как в LaunchedEffect)
 * @param topBar Composable функция для top app bar
 */
@Composable
fun ScaffoldState.ConfigureTopBar(
    vararg key: Any?,
    topBar: @Composable () -> Unit
) {
    LaunchedEffect(*key) {
        updateConfig {
            copy(topBar = topBar)
        }
    }
}

/**
 * Декларативно устанавливает bottom bar при входе на экран.
 * MainScaffold автоматически очистит конфигурацию при смене route.
 *
 * @param bottomBar Composable функция для bottom navigation bar
 */
@Composable
fun ScaffoldState.SetupBottomBar(bottomBar: @Composable () -> Unit) {
    LaunchedEffect(Unit) {
        updateBottomBar(bottomBar)
    }
}

/**
 * Устанавливает bottom bar с ключом для отслеживания изменений.
 *
 * @param key Ключи для отслеживания изменений
 * @param bottomBar Composable функция для bottom navigation bar
 */
@Composable
fun ScaffoldState.ConfigureBottomBar(
    vararg key: Any?,
    bottomBar: @Composable () -> Unit
) {
    LaunchedEffect(*key) {
        updateConfig {
            copy(bottomBar = bottomBar)
        }
    }
}

/**
 * Декларативно устанавливает FAB при входе на экран.
 * MainScaffold автоматически очистит конфигурацию при смене route.
 *
 * @param position Позиция FAB (по умолчанию End)
 * @param fab Composable функция для floating action button
 */
@Composable
fun ScaffoldState.SetupFAB(
    position: FabPosition = FabPosition.End,
    fab: @Composable () -> Unit
) {
    LaunchedEffect(Unit) {
        updateConfig {
            copy(
                floatingActionButton = fab,
                floatingActionButtonPosition = position
            )
        }
    }
}

/**
 * Устанавливает FAB с ключом для отслеживания изменений.
 *
 * @param key Ключи для отслеживания изменений
 * @param position Позиция FAB (по умолчанию End)
 * @param fab Composable функция для floating action button
 */
@Composable
fun ScaffoldState.ConfigureFAB(
    vararg key: Any?,
    position: FabPosition = FabPosition.End,
    fab: @Composable () -> Unit
) {
    LaunchedEffect(*key) {
        updateConfig {
            copy(
                floatingActionButton = fab,
                floatingActionButtonPosition = position
            )
        }
    }
}

/**
 * Декларативно устанавливает полную конфигурацию scaffold при входе на экран.
 * MainScaffold автоматически очистит конфигурацию при смене route.
 *
 * @param config Функция обновления конфигурации
 */
@Composable
fun ScaffoldState.SetupScaffold(config: ScaffoldConfig.() -> ScaffoldConfig) {
    LaunchedEffect(Unit) {
        updateConfig(config)
    }
}

/**
 * Императивно обновляет scaffold конфигурацию в LaunchedEffect.
 * Используется когда нужно обновить scaffold в ответ на изменение state.
 *
 * Использование:
 * ```kotlin
 * LaunchedEffect(isEditMode) {
 *     scaffoldState.configure {
 *         copy(
 *             topBar = if (isEditMode) { EditTopBar() } else { ViewTopBar() }
 *         )
 *     }
 * }
 * ```
 *
 * @param key Ключи для отслеживания изменений (как в LaunchedEffect)
 * @param config Функция обновления конфигурации
 */
@Composable
fun ScaffoldState.Configure(
    vararg key: Any?,
    config: ScaffoldConfig.() -> ScaffoldConfig
) {
    LaunchedEffect(*key) {
        updateConfig(config)
    }
}

/**
 * Показывает только bottom bar, скрывая остальные элементы scaffold.
 * Удобно для экранов верхнего уровня навигации.
 * MainScaffold автоматически очистит конфигурацию при смене route.
 *
 * @param bottomBar Composable функция для bottom navigation bar
 */
@Composable
fun ScaffoldState.ShowOnlyBottomBar(bottomBar: @Composable () -> Unit) {
    LaunchedEffect(Unit) {
        updateConfig {
            ScaffoldConfig(bottomBar = bottomBar)
        }
    }
}

/**
 * Показывает только top bar, скрывая остальные элементы scaffold.
 * Удобно для экранов деталей с навигацией назад.
 * MainScaffold автоматически очистит конфигурацию при смене route.
 *
 * @param topBar Composable функция для top app bar
 */
@Composable
fun ScaffoldState.ShowOnlyTopBar(topBar: @Composable () -> Unit) {
    LaunchedEffect(Unit) {
        updateConfig {
            ScaffoldConfig(topBar = topBar)
        }
    }
}

/**
 * Скрывает все элементы scaffold (чистый экран без bars).
 * Используется для fullscreen экранов (splash, onboarding, etc).
 */
@Composable
fun ScaffoldState.HideAll() {
    LaunchedEffect(Unit) {
        reset()
    }
}
