package com.example.amulet.feature.practices.presentation.details

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.patterns.model.Pattern
import com.example.amulet.shared.domain.practices.model.Practice
import com.example.amulet.shared.domain.courses.model.Course
import com.example.amulet.shared.domain.devices.model.BleConnectionState

data class PracticeDetailsState(
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val practiceId: String? = null,
    val practice: Practice? = null,
    val pattern: Pattern? = null,
    val isFavorite: Boolean = false,
    val courses: List<Course> = emptyList(),
    val connectionStatus: BleConnectionState = BleConnectionState.Disconnected
)

sealed class PracticeDetailsIntent {
    object StartPractice : PracticeDetailsIntent()
    object ToggleFavorite : PracticeDetailsIntent()
    object OpenPattern : PracticeDetailsIntent()
    object AddToPlan : PracticeDetailsIntent()
    data class OpenCourse(val courseId: String) : PracticeDetailsIntent()
    object OpenPairing : PracticeDetailsIntent()
    object NavigateBack : PracticeDetailsIntent()
    object OpenEditor : PracticeDetailsIntent()
}

sealed class PracticeDetailsEffect {
    data class NavigateToPattern(val patternId: String) : PracticeDetailsEffect()
    object NavigateBack : PracticeDetailsEffect()
    data class NavigateToPlan(val practiceId: String) : PracticeDetailsEffect()
    data class NavigateToCourse(val courseId: String) : PracticeDetailsEffect()
    data class NavigateToSession(val practiceId: String) : PracticeDetailsEffect()
    object NavigateToPairing : PracticeDetailsEffect()
    data class NavigateToEditor(val practiceId: String) : PracticeDetailsEffect()
}
