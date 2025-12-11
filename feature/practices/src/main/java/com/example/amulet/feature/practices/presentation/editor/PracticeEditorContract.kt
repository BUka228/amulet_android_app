package com.example.amulet.feature.practices.presentation.editor

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.patterns.model.Pattern
import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.patterns.model.PatternWithSegments
import com.example.amulet.shared.domain.practices.model.PracticeType

data class PracticeEditorState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: AppError? = null,
    val practiceId: String? = null,
    val editorPractice: EditorPractice = EditorPractice(),
    val basePatternWithSegments: PatternWithSegments? = null,
    val basePatternLoading: Boolean = false,
    val availablePatterns: List<Pattern> = emptyList(),
    val isPatternSheetVisible: Boolean = false,
    val selectedStepOrder: Int? = null,
)

sealed interface PracticeEditorEvent {
    data object InitIfNeeded : PracticeEditorEvent
    data class UpdateTitle(val title: String) : PracticeEditorEvent
    data class UpdateType(val type: PracticeType) : PracticeEditorEvent
    data class UpdateTargetDuration(val durationSec: Int?) : PracticeEditorEvent
    data class UpdateAbout(val text: String) : PracticeEditorEvent
    data class UpdateHowItGoes(val text: String) : PracticeEditorEvent
    data class UpdateSafetyNotes(val text: String) : PracticeEditorEvent
    data object TogglePatternSheet : PracticeEditorEvent
    data class SetBasePattern(val patternId: PatternId) : PracticeEditorEvent
    data object GenerateStepsFromSegments : PracticeEditorEvent
    data class UpdateStepTitle(val order: Int, val title: String) : PracticeEditorEvent
    data class UpdateStepDescription(val order: Int, val description: String) : PracticeEditorEvent
    data class UpdateStepDuration(val order: Int, val durationSec: Int?) : PracticeEditorEvent
    data class UpdateSegmentGroupRepeat(val order: Int, val repeatCount: Int) : PracticeEditorEvent
    data class MergeStepWithNext(val order: Int) : PracticeEditorEvent
    data class ToggleSegmentInStep(val order: Int, val segmentIndex: Int) : PracticeEditorEvent
    data object AddStep : PracticeEditorEvent
    data class DeleteStep(val order: Int) : PracticeEditorEvent
    data class SelectStep(val order: Int?) : PracticeEditorEvent
    data object Save : PracticeEditorEvent
    data object DismissError : PracticeEditorEvent
    data object OnBackClick : PracticeEditorEvent
}

sealed interface PracticeEditorEffect {
    data object NavigateBack : PracticeEditorEffect
    data class ShowMessage(val message: String) : PracticeEditorEffect
}
