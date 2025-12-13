package com.example.amulet.feature.hugs.presentation.emotions

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.hugs.model.Pair
import com.example.amulet.shared.domain.hugs.model.PairEmotion
import com.example.amulet.shared.domain.user.model.User

sealed class HugsEmotionsIntent {
    data class ToggleSelection(val emotionId: String) : HugsEmotionsIntent()
    object ClearSelection : HugsEmotionsIntent()
    object DeleteSelected : HugsEmotionsIntent()
}

data class HugsEmotionsState(
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    val error: AppError? = null,
    val currentUser: User? = null,
    val activePair: Pair? = null,
    val emotions: List<PairEmotion> = emptyList(),
    val patternTitles: Map<String, String> = emptyMap(),
    val selectedEmotionIds: Set<String> = emptySet(),
)
