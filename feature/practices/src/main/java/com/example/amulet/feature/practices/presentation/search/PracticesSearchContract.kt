package com.example.amulet.feature.practices.presentation.search

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.practices.model.Practice
import com.example.amulet.shared.domain.practices.model.PracticeFilter
import com.example.amulet.shared.domain.practices.model.PracticeGoal
import com.example.amulet.shared.domain.practices.model.PracticeLevel
import com.example.amulet.shared.domain.practices.model.PracticeType

import com.example.amulet.shared.domain.courses.model.Course

enum class SearchResultHeaderType {
    COURSES,
    PRACTICES
}

sealed interface SearchResultItem {
    data class HeaderItem(val type: SearchResultHeaderType) : SearchResultItem
    data class PracticeItem(val practice: Practice) : SearchResultItem
    data class CourseItem(val course: Course) : SearchResultItem
}

data class PracticesSearchState(
    val query: String = "",
    val isLoading: Boolean = false,
    val error: AppError? = null,
    val results: List<SearchResultItem> = emptyList(),
    val filter: PracticeFilter = PracticeFilter(),
    val isAdvancedFiltersVisible: Boolean = false,
    val recommendations: List<Practice> = emptyList(),
    val isLoadingRecommendations: Boolean = false,
    val isQuickFiltersExpanded: Boolean = true,
    val isCoursesFilterSelected: Boolean = false
)

sealed interface PracticesSearchEvent {
    data class OnQueryChange(val query: String) : PracticesSearchEvent
    data class OnTypeFilterChange(val type: PracticeType?) : PracticesSearchEvent
    data class OnGoalFilterChange(val goal: PracticeGoal?) : PracticesSearchEvent
    data class OnLevelFilterChange(val level: PracticeLevel?) : PracticesSearchEvent
    data class OnDurationRangeChange(val fromSec: Int?, val toSec: Int?) : PracticesSearchEvent
    data class OnHasAudioChange(val hasAudio: Boolean?) : PracticesSearchEvent
    data class OnAmuletRequiredChange(val amuletRequired: Boolean?) : PracticesSearchEvent
    data class OnCoursesFilterChange(val selected: Boolean) : PracticesSearchEvent
    data object OnToggleAdvancedFilters : PracticesSearchEvent
    data object OnToggleQuickFilters : PracticesSearchEvent
    data object OnClearFilters : PracticesSearchEvent
}
