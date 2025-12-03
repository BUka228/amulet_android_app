package com.example.amulet.feature.hugs.presentation.emotions

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.hugs.model.Pair
import com.example.amulet.shared.domain.hugs.model.PairEmotion
import com.example.amulet.shared.domain.user.model.User

data class HugsEmotionsState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: AppError? = null,
    val currentUser: User? = null,
    val activePair: Pair? = null,
    val emotions: List<PairEmotion> = emptyList(),
    val editingEmotion: PairEmotion? = null,
)

sealed class HugsEmotionsIntent {
    data class EditEmotion(val emotionId: String) : HugsEmotionsIntent()
    object CancelEdit : HugsEmotionsIntent()
    data class ChangeEditingName(val value: String) : HugsEmotionsIntent()
    data class ChangeEditingColor(val colorHex: String) : HugsEmotionsIntent()
    object SaveEditing : HugsEmotionsIntent()
    data class OpenPatternEditor(val patternId: String?) : HugsEmotionsIntent()
}

sealed class HugsEmotionsEffect {
    data class ShowError(val error: AppError) : HugsEmotionsEffect()
    data class OpenPatternEditor(val patternId: String?) : HugsEmotionsEffect()
}
