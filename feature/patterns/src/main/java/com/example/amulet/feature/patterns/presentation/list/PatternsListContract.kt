package com.example.amulet.feature.patterns.presentation.list

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.patterns.model.Pattern
import com.example.amulet.shared.domain.patterns.model.PatternKind

/**
 * Контракт для экрана списка паттернов.
 */

data class PatternsListState(
    val patterns: List<Pattern> = emptyList(),
    val myPatterns: List<Pattern> = emptyList(),
    val publicPatterns: List<Pattern> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isEmpty: Boolean = false,
    val selectedTab: PatternTab = PatternTab.MY_PATTERNS,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val error: AppError? = null
)

enum class PatternTab {
    MY_PATTERNS,
    PUBLIC,
    PRESETS
}

sealed interface PatternsListEvent {
    data object Refresh : PatternsListEvent
    data class SelectTab(val tab: PatternTab) : PatternsListEvent
    data class UpdateSearchQuery(val query: String) : PatternsListEvent
    data class PatternClicked(val patternId: String) : PatternsListEvent
    data object CreatePatternClicked : PatternsListEvent
    data class DeletePattern(val patternId: String) : PatternsListEvent
    data class PreviewPattern(val patternId: String) : PatternsListEvent
    data object ToggleSearch : PatternsListEvent
    data object DismissError : PatternsListEvent
}

sealed interface PatternsListSideEffect {
    data class NavigateToPatternEditor(val patternId: String?) : PatternsListSideEffect
    data class NavigateToPatternPreview(val patternId: String) : PatternsListSideEffect
    data class ShowSnackbar(val message: String) : PatternsListSideEffect
    data class ShowDeleteConfirmation(val patternId: String) : PatternsListSideEffect
}
