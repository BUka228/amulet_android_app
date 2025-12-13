package com.example.amulet.feature.hugs.presentation.emotions.editor

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.hugs.model.PairEmotion

data class HugsEmotionEditorState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: AppError? = null,
    val emotionId: String? = null,
    val emotion: PairEmotion? = null,
    val selectedPatternTitle: String? = null,
)

sealed class HugsEmotionEditorIntent {
    data class ChangeName(val value: String) : HugsEmotionEditorIntent()
    data class ChangeColor(val colorHex: String) : HugsEmotionEditorIntent()
    data class ChangePattern(val patternId: String?) : HugsEmotionEditorIntent()
    object OpenPatternPicker : HugsEmotionEditorIntent()
    object Save : HugsEmotionEditorIntent()
}

sealed class HugsEmotionEditorEffect {
    data class ShowError(val error: AppError) : HugsEmotionEditorEffect()
    object OpenPatternPicker : HugsEmotionEditorEffect()
    object NavigateBack : HugsEmotionEditorEffect()
}
