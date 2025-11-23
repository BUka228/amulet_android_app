package com.example.amulet.feature.practices.presentation.course

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.courses.model.Course
import com.example.amulet.shared.domain.courses.model.CourseItem
import com.example.amulet.shared.domain.courses.model.CourseModule
import com.example.amulet.shared.domain.courses.model.CourseProgress
import com.example.amulet.shared.domain.practices.model.ScheduledSession

data class CourseDetailsState(
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val courseId: String? = null,
    val course: Course? = null,
    val items: List<CourseItem> = emptyList(),
    val modules: List<CourseModule> = emptyList(),
    val progress: CourseProgress? = null,
    val scheduledSessions: List<ScheduledSession> = emptyList(),
    val expandedModuleIds: Set<String> = emptySet(),
    val unlockedItemIds: Set<String> = emptySet()
)

sealed interface CourseDetailsEvent {
    data class OnModuleClick(val moduleId: String) : CourseDetailsEvent
    data class OnPracticeClick(val practiceId: String) : CourseDetailsEvent
    data object OnStartCourse : CourseDetailsEvent
    data object OnContinueCourse : CourseDetailsEvent
    data object OnResetCourse : CourseDetailsEvent
    data object OnNavigateBack : CourseDetailsEvent
}
