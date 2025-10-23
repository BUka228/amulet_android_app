package com.example.amulet.core.design.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * State holder для централизованного управления Scaffold.
 * Позволяет любому экрану обновлять конфигурацию scaffold без вложенности.
 *
 * Enterprise-паттерн: Centralized State Management для UI компонентов.
 *
 * Использование:
 * ```kotlin
 * val scaffoldState = rememberScaffoldState()
 *
 * LaunchedEffect(Unit) {
 *     scaffoldState.updateConfig {
 *         copy(
 *             topBar = { MyTopAppBar() },
 *             bottomBar = { MyBottomBar() }
 *         )
 *     }
 * }
 * ```
 *
 * @property config Текущая конфигурация scaffold
 */
@Stable
class ScaffoldState {
    /**
     * Текущая конфигурация scaffold.
     * Используется для чтения в UI.
     */
    var config by mutableStateOf(ScaffoldConfig.Empty)
        private set

    /**
     * Обновить всю конфигурацию scaffold.
     *
     * @param update Функция обновления, принимающая текущий config и возвращающая новый
     */
    fun updateConfig(update: ScaffoldConfig.() -> ScaffoldConfig) {
        config = config.update()
    }

    /**
     * Сбросить конфигурацию к пустой.
     * Полезно при выходе с экрана или при навигации.
     */
    fun reset() {
        config = ScaffoldConfig.Empty
    }

    /**
     * Обновить только top bar.
     *
     * @param topBar Новый composable для top app bar
     */
    fun updateTopBar(topBar: @Composable () -> Unit) {
        config = config.copy(topBar = topBar)
    }

    /**
     * Обновить только bottom bar.
     *
     * @param bottomBar Новый composable для bottom navigation bar
     */
    fun updateBottomBar(bottomBar: @Composable () -> Unit) {
        config = config.copy(bottomBar = bottomBar)
    }

    /**
     * Обновить только FAB.
     *
     * @param fab Новый composable для floating action button
     */
    fun updateFab(fab: @Composable () -> Unit) {
        config = config.copy(floatingActionButton = fab)
    }

    /**
     * Обновить только snackbar host.
     *
     * @param snackbarHost Новый composable для snackbar host
     */
    fun updateSnackbarHost(snackbarHost: @Composable () -> Unit) {
        config = config.copy(snackbarHost = snackbarHost)
    }
}

/**
 * Remember scaffold state.
 * Создаёт и запоминает ScaffoldState для использования в композиции.
 *
 * @return ScaffoldState instance
 */
@Composable
fun rememberScaffoldState(): ScaffoldState {
    return remember { ScaffoldState() }
}
