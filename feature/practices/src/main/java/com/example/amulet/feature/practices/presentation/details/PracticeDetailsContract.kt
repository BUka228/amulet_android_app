package com.example.amulet.feature.practices.presentation.details

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.patterns.model.Pattern
import com.example.amulet.shared.domain.practices.model.Practice

data class PracticeDetailsState(
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val practiceId: String? = null,
    val practice: Practice? = null,
    val pattern: Pattern? = null,
    val isFavorite: Boolean = false
)

sealed class PracticeDetailsIntent {
    object StartPractice : PracticeDetailsIntent()
    object ToggleFavorite : PracticeDetailsIntent()
    object OpenPattern : PracticeDetailsIntent()
    object AddToPlan : PracticeDetailsIntent()
    object NavigateBack : PracticeDetailsIntent()
}

sealed class PracticeDetailsEffect {
    data class NavigateToPattern(val patternId: String) : PracticeDetailsEffect()
    object NavigateBack : PracticeDetailsEffect()
    data class NavigateToPlan(val practiceId: String) : PracticeDetailsEffect()
}
