package com.example.amulet.core.design.scaffold

import androidx.compose.material3.FabPosition
import androidx.compose.runtime.Composable

/**
 * Extension functions для работы со ScaffoldState.
 * 
 * УПРОЩЕННЫЙ ПОДХОД: прямые вызовы updateConfig в SideEffect для синхронного выполнения.
 * 
 * **КРИТИЧЕСКИ ВАЖНО**: всегда оборачивать updateConfig в SideEffect!
 * Это гарантирует:
 * - Немедленное синхронное выполнение
 * - Отсутствие задержек при быстрой навигации
 * - Корректную работу при рекомпозиции
 * 
 * Пример использования в экране:
 * ```kotlin
 * @Composable
 * fun MyScreen() {
 *     val scaffoldState = LocalScaffoldState.current
 *     
 *     // ✅ ПРАВИЛЬНО: updateConfig в SideEffect
 *     SideEffect {
 *         scaffoldState.updateConfig {
 *             copy(
 *                 topBar = {
 *                     TopAppBar(
 *                         title = { Text("My Screen") },
 *                         navigationIcon = { 
 *                             IconButton(onClick = onBack) {
 *                                 Icon(Icons.Default.ArrowBack, null)
 *                             }
 *                         }
 *                     )
 *                 },
 *                 floatingActionButton = {} // Явно обнуляем FAB
 *             )
 *         }
 *     }
 *     
 *     // Контент экрана
 *     MyContent()
 * }
 * ```
 * 
 * ❌ НЕПРАВИЛЬНО - вызов напрямую:
 * ```kotlin
 * scaffoldState.updateConfig { ... } // Будет задержка!
 * ```
 * 
 * Правило: каждый экран точечно указывает что нужно, обнуляя ненужное:
 * - Основные экраны с bottom bar: обнуляем topBar и FAB
 * - Детальные экраны: устанавливаем topBar, обнуляем FAB
 * - Списки с действиями: устанавливаем topBar и FAB
 */

/**
 * Хелпер для установки только topBar, сбрасывая FAB.
 * Используется для экранов с навигацией назад.
 */
fun ScaffoldState.setTopBarOnly(topBar: @Composable () -> Unit) {
    updateConfig {
        copy(
            topBar = topBar,
            floatingActionButton = {}
        )
    }
}

/**
 * Хелпер для установки topBar и FAB.
 * Используется для экранов со списками и действиями.
 */
fun ScaffoldState.setTopBarWithFab(
    topBar: @Composable () -> Unit,
    fab: @Composable () -> Unit,
    fabPosition: FabPosition = FabPosition.End
) {
    updateConfig {
        copy(
            topBar = topBar,
            floatingActionButton = fab,
            floatingActionButtonPosition = fabPosition
        )
    }
}
