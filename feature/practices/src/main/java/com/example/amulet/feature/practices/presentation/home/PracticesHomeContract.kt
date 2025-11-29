package com.example.amulet.feature.practices.presentation.home

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.practices.model.Practice
import com.example.amulet.shared.domain.practices.model.PracticeCategory
import com.example.amulet.shared.domain.practices.model.PracticeSession
import com.example.amulet.shared.domain.practices.model.ScheduledSession
import com.example.amulet.shared.domain.courses.model.Course
import com.example.amulet.shared.domain.courses.model.CourseProgress
import com.example.amulet.shared.domain.practices.model.PracticeFilter
import com.example.amulet.shared.domain.practices.model.PracticeType
import com.example.amulet.shared.domain.practices.model.PracticeGoal
import com.example.amulet.shared.domain.practices.model.MoodKind

data class PracticesHomeState(
    val isLoading: Boolean = true,
    val error: AppError? = null,

    val greeting: String = "",
    val selectedMood: MoodKind = MoodKind.NEUTRAL,
    val availableMoods: List<MoodKind> = listOf(
        MoodKind.NERVOUS,
        MoodKind.SLEEP,
        MoodKind.FOCUS,
        MoodKind.RELAX,
        MoodKind.HAPPY,
        MoodKind.SAD,
        MoodKind.ANGRY,
        MoodKind.TIRED,
    ),

    val recommendedPractices: List<Practice> = emptyList(),
    val recommendedCourse: Course? = null,

    val myCourses: List<Course> = emptyList(),
    val coursesProgress: Map<String, CourseProgress> = emptyMap(),
    val quickRituals: List<Practice> = emptyList(),
    val recentSessions: List<RecentSessionUi> = emptyList(),
    val scheduledSessions: List<ScheduledSession> = emptyList(),

    val hasPlan: Boolean = false,
    val isNewUser: Boolean = false,
    val isSearchMode: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<Practice> = emptyList(),

    val isRefreshing: Boolean = false,
    val isRecommendationsLoading: Boolean = false,
    val isCoursesLoading: Boolean = false,
    val isQuickRitualsLoading: Boolean = false,
    val isRecentLoading: Boolean = false,

    val recommendationsError: AppError? = null,
    val coursesError: AppError? = null,
    val quickRitualsError: AppError? = null,
    val recentError: AppError? = null,
)

sealed class PracticesHomeIntent {
    data class SelectMood(val mood: MoodKind) : PracticesHomeIntent()
    object SaveSelectedMood : PracticesHomeIntent()

    object Refresh : PracticesHomeIntent()

    data class OpenPractice(val practiceId: String) : PracticesHomeIntent()
    data class OpenCourse(val courseId: String) : PracticesHomeIntent()

    data class ToggleFavorite(val practiceId: String, val favorite: Boolean) : PracticesHomeIntent()

    object OpenSchedule : PracticesHomeIntent()
    object OpenStats : PracticesHomeIntent()
    object OpenSearch : PracticesHomeIntent()
    object CreateDayRitual : PracticesHomeIntent()

    data class RescheduleSession(val sessionId: String) : PracticesHomeIntent()
    data class CancelSession(val sessionId: String) : PracticesHomeIntent()
    data class ShowPracticeDetails(val practiceId: String) : PracticesHomeIntent()
    data class OpenPracticeSession(val practiceId: String) : PracticesHomeIntent()
}

sealed class PracticesHomeEffect {
    data class NavigateToPractice(val practiceId: String) : PracticesHomeEffect()
    data class NavigateToCourse(val courseId: String) : PracticesHomeEffect()

    data class NavigateToPracticeSession(val practiceId: String) : PracticesHomeEffect()

    object NavigateToSchedule : PracticesHomeEffect()
    object NavigateToStats : PracticesHomeEffect()
    object NavigateToSearch : PracticesHomeEffect()

    data class ShowError(val error: AppError) : PracticesHomeEffect()
}
