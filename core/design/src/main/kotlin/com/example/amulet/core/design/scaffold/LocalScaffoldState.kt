package com.example.amulet.core.design.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocal для доступа к ScaffoldState из любой точки композиции.
 * Enterprise-паттерн: Dependency Injection через Composition.
 *
 * Использование в экране:
 * ```kotlin
 * @Composable
 * fun MyScreen() {
 *     val scaffoldState = LocalScaffoldState.current
 *
 *     scaffoldState.SetupTopBar {
 *         TopAppBar(title = { Text("My Screen") })
 *     }
 *
 *     // Контент экрана
 * }
 * ```
 */
val LocalScaffoldState = staticCompositionLocalOf<ScaffoldState> {
    error("ScaffoldState not provided. Wrap your app with ProvideScaffoldState.")
}

/**
 * Провайдер для ScaffoldState.
 * Должен оборачивать весь navigation graph на уровне приложения.
 *
 * @param scaffoldState Экземпляр ScaffoldState для предоставления
 * @param content Композиция, которая получит доступ к scaffoldState
 */
@Composable
fun ProvideScaffoldState(
    scaffoldState: ScaffoldState,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalScaffoldState provides scaffoldState,
        content = content
    )
}
