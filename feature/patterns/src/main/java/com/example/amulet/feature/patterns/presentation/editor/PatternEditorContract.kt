package com.example.amulet.feature.patterns.presentation.editor

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.patterns.model.Pattern
import com.example.amulet.shared.domain.patterns.model.PatternElement
import com.example.amulet.shared.domain.patterns.model.PatternKind
import com.example.amulet.shared.domain.patterns.model.PatternSpec

/**
 * Контракт для экрана редактора паттернов.
 */

data class PatternEditorState(
    val pattern: Pattern? = null,
    val patternId: String? = null,
    val title: String = "",
    val description: String = "",
    val kind: PatternKind = PatternKind.LIGHT,
    val hardwareVersion: Int = 100,
    val loop: Boolean = false,
    val elements: List<PatternElement> = emptyList(),
    val selectedElementIndex: Int? = null,
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val error: AppError? = null,
    val validationErrors: Map<String, String> = emptyMap()
)

sealed interface PatternEditorEvent {
    data object LoadPattern : PatternEditorEvent
    data class UpdateTitle(val title: String) : PatternEditorEvent
    data class UpdateDescription(val description: String) : PatternEditorEvent
    data class UpdateKind(val kind: PatternKind) : PatternEditorEvent
    data class UpdateLoop(val loop: Boolean) : PatternEditorEvent
    data class AddElement(val element: PatternElement) : PatternEditorEvent
    data class UpdateElement(val index: Int, val element: PatternElement) : PatternEditorEvent
    data class RemoveElement(val index: Int) : PatternEditorEvent
    data class MoveElement(val from: Int, val to: Int) : PatternEditorEvent
    data class SelectElement(val index: Int?) : PatternEditorEvent
    data object SavePattern : PatternEditorEvent
    data object PublishPattern : PatternEditorEvent
    data object PreviewPattern : PatternEditorEvent
    data object DiscardChanges : PatternEditorEvent
    data object DismissError : PatternEditorEvent
}

sealed interface PatternEditorSideEffect {
    data class ShowSnackbar(val message: String) : PatternEditorSideEffect
    data object NavigateBack : PatternEditorSideEffect
    data class NavigateToPreview(val spec: PatternSpec) : PatternEditorSideEffect
    data object ShowDiscardConfirmation : PatternEditorSideEffect
    data object ShowPublishDialog : PatternEditorSideEffect
    data object ShowElementPicker : PatternEditorSideEffect
}
