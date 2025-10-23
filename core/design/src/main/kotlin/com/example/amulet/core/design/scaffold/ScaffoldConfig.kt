package com.example.amulet.core.design.scaffold

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.FabPosition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Конфигурация для централизованного управления Scaffold.
 * Используется для настройки topBar, bottomBar, FAB и других параметров scaffold
 * из любого экрана без вложенности.
 *
 * Enterprise-паттерн: Single Source of Truth для UI scaffold компонентов.
 *
 * @property topBar Composable функция для top app bar
 * @property bottomBar Composable функция для bottom navigation bar
 * @property floatingActionButton Composable функция для FAB
 * @property floatingActionButtonPosition Позиция FAB (End, Center, Start)
 * @property snackbarHost Composable функция для snackbar host
 * @property containerColor Цвет контейнера scaffold
 * @property contentColor Цвет контента scaffold
 * @property contentWindowInsets Window insets для корректной работы с system bars
 */
@Immutable
data class ScaffoldConfig(
    val topBar: @Composable () -> Unit = {},
    val bottomBar: @Composable () -> Unit = {},
    val floatingActionButton: @Composable () -> Unit = {},
    val floatingActionButtonPosition: FabPosition = FabPosition.End,
    val snackbarHost: @Composable () -> Unit = {},
    val containerColor: Color = Color.Unspecified,
    val contentColor: Color = Color.Unspecified,
    val contentWindowInsets: WindowInsets = WindowInsets(0, 0, 0, 0)
) {
    companion object {
        /**
         * Пустая конфигурация - scaffold без дополнительных элементов.
         * Используется по умолчанию для экранов без topBar/bottomBar.
         */
        val Empty = ScaffoldConfig()
    }
}
