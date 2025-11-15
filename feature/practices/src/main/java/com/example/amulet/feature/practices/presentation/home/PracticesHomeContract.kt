package com.example.amulet.feature.practices.presentation.home

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.practices.model.Practice
import com.example.amulet.shared.domain.practices.model.PracticeCategory
import com.example.amulet.shared.domain.practices.model.PracticeSession
import com.example.amulet.shared.domain.courses.model.Course
import com.example.amulet.shared.domain.practices.model.PracticeFilter
import com.example.amulet.shared.domain.practices.model.PracticeType

data class PracticesHomeState(
    val isLoadingOverview: Boolean = true,
    val isLoadingCourses: Boolean = true,
    val error: AppError? = null,
    val selectedTab: PracticesTab = PracticesTab.Overview,
    val filters: PracticeFilter = PracticeFilter(),
    val categories: List<PracticeCategory> = emptyList(),
    val practices: List<Practice> = emptyList(),
    val recommendations: List<Practice> = emptyList(),
    val recent: List<Practice> = emptyList(),
    val favorites: List<Practice> = emptyList(),
    val activeSession: PracticeSession? = null,
    val searchQuery: String = "",
    val courses: List<Course> = emptyList(),
    val continueCourses: List<Course> = emptyList()
)

enum class PracticesTab { Overview, Courses, Favorites }

sealed interface PracticesHomeEvent {
    data object Refresh : PracticesHomeEvent
    data class SelectTab(val tab: PracticesTab) : PracticesHomeEvent
    data class UpdateSearchQuery(val query: String) : PracticesHomeEvent
    data class ApplyFilters(
        val type: PracticeType? = null,
        val categoryId: String? = null,
        val onlyFavorites: Boolean = false,
        val durationFromSec: Int? = null,
        val durationToSec: Int? = null
    ) : PracticesHomeEvent
    data object ClearFilters : PracticesHomeEvent
    data class SelectCategory(val categoryId: String?) : PracticesHomeEvent
    data class ToggleFavorite(val practiceId: String, val favorite: Boolean) : PracticesHomeEvent
    data class StartPractice(val practiceId: String) : PracticesHomeEvent
    data class PauseSession(val sessionId: String) : PracticesHomeEvent
    data class ResumeSession(val sessionId: String) : PracticesHomeEvent
    data class StopSession(val sessionId: String, val completed: Boolean) : PracticesHomeEvent
    data class OpenPractice(val practiceId: String) : PracticesHomeEvent
    data class OpenCourse(val courseId: String) : PracticesHomeEvent
    data object DismissError : PracticesHomeEvent
}

sealed interface PracticesHomeSideEffect {
    data class NavigateToPracticeDetails(val practiceId: String) : PracticesHomeSideEffect
    data class NavigateToCourseDetails(val courseId: String) : PracticesHomeSideEffect
    data class ShowSnackbar(val message: String) : PracticesHomeSideEffect
}

