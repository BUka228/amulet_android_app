package com.example.amulet.feature.patterns.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.amulet.feature.patterns.presentation.editor.PatternEditorRoute
import com.example.amulet.feature.patterns.presentation.list.PatternsListRoute
import com.example.amulet.feature.patterns.presentation.preview.PatternPreviewRoute
import com.example.amulet.shared.domain.patterns.PreviewCache
import com.example.amulet.shared.domain.patterns.model.PatternSpec
import java.util.UUID

object PatternsGraph {
    const val route: String = "patterns_graph"
}

object PatternsDestination {
    const val list: String = "patterns/list"
    const val editor: String = "patterns/editor?patternId={patternId}"
    const val preview: String = "patterns/preview/{patternId}"
}

fun NavController.navigateToPatternsList(popUpToInclusive: Boolean = false) {
    navigate(PatternsDestination.list) {
        if (popUpToInclusive) {
            popUpTo(PatternsDestination.list) { inclusive = true }
        }
        launchSingleTop = true
    }
}

fun NavController.navigateToPatternEditor(patternId: String? = null) {
    val route = if (patternId != null) {
        "patterns/editor?patternId=$patternId"
    } else {
        "patterns/editor"
    }
    navigate(route) {
        launchSingleTop = true
    }
}

fun NavController.navigateToPatternPreview(patternId: String) {
    navigate("patterns/preview/$patternId") {
        launchSingleTop = true
    }
}

fun NavController.navigateToPatternPreview(spec: PatternSpec) {
    val key = UUID.randomUUID().toString()
    PreviewCache.put(key, spec)
    navigate("patterns/preview?key=$key") {
        launchSingleTop = true
    }
}

fun NavGraphBuilder.patternsGraph(
    navController: NavController,
) {
    // Константы для анимаций
    val animationDuration = 300
    
    navigation(startDestination = PatternsDestination.list, route = PatternsGraph.route) {
        // Список паттернов
        composable(
            route = PatternsDestination.list,

        ) {
            PatternsListRoute(
                onNavigateToEditor = { patternId ->
                    navController.navigateToPatternEditor(patternId)
                },
                onNavigateToPreview = { patternId ->
                    navController.navigateToPatternPreview(patternId)
                }
            )
        }

        // Редактор паттернов
        composable(
            route = PatternsDestination.editor,
            arguments = listOf(
                navArgument("patternId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            ),
            
        ) {
            PatternEditorRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPreview = navController::navigateToPatternPreview
            )
        }

        // Предпросмотр паттерна
        composable(
            route = PatternsDestination.preview,
            arguments = listOf(
                navArgument("patternId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("key") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val patternId = backStackEntry.arguments?.getString("patternId")
            val previewKey = backStackEntry.arguments?.getString("key")
            PatternPreviewRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditor = {
                    patternId?.let { navController.navigateToPatternEditor(it) }
                }
            )
        }
    }
}
