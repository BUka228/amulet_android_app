package com.example.amulet.feature.patterns.presentation.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.domain.patterns.model.*
import com.example.amulet.shared.domain.patterns.usecase.*
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatternEditorViewModel @Inject constructor(
    private val createPatternUseCase: CreatePatternUseCase,
    private val updatePatternUseCase: UpdatePatternUseCase,
    private val getPatternByIdUseCase: GetPatternByIdUseCase,
    private val publishPatternUseCase: PublishPatternUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val patternId: String? = savedStateHandle.get<String>("patternId")

    private val _uiState = MutableStateFlow(PatternEditorState(patternId = patternId))
    val uiState: StateFlow<PatternEditorState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<PatternEditorSideEffect>()
    val sideEffect: SharedFlow<PatternEditorSideEffect> = _sideEffect.asSharedFlow()

    init {
        if (patternId != null) {
            loadPattern()
        }
    }

    fun handleEvent(event: PatternEditorEvent) {
        when (event) {
            is PatternEditorEvent.LoadPattern -> loadPattern()
            is PatternEditorEvent.UpdateTitle -> updateTitle(event.title)
            is PatternEditorEvent.UpdateDescription -> updateDescription(event.description)
            is PatternEditorEvent.UpdateKind -> updateKind(event.kind)
            is PatternEditorEvent.UpdateLoop -> updateLoop(event.loop)
            is PatternEditorEvent.ShowElementPicker -> showElementPicker()
            is PatternEditorEvent.AddElement -> addElement(event.element)
            is PatternEditorEvent.UpdateElement -> updateElement(event.index, event.element)
            is PatternEditorEvent.RemoveElement -> removeElement(event.index)
            is PatternEditorEvent.MoveElement -> moveElement(event.from, event.to)
            is PatternEditorEvent.SelectElement -> selectElement(event.index)
            is PatternEditorEvent.SavePattern -> savePattern()
            is PatternEditorEvent.PublishPattern -> publishPattern()
            is PatternEditorEvent.PreviewPattern -> previewPattern()
            is PatternEditorEvent.DiscardChanges -> discardChanges()
            is PatternEditorEvent.DismissError -> dismissError()
        }
    }

    private fun loadPattern() {
        if (patternId == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getPatternByIdUseCase(PatternId(patternId))
                .collect { pattern ->
                    pattern?.let {
                        _uiState.update { state ->
                            state.copy(
                                pattern = it,
                                title = it.title,
                                description = it.description ?: "",
                                kind = it.kind,
                                hardwareVersion = it.hardwareVersion,
                                loop = it.spec.loop,
                                elements = it.spec.elements,
                                isLoading = false,
                                isEditing = true
                            )
                        }
                    } ?: run {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
        }
    }

    private fun updateTitle(title: String) {
        _uiState.update {
            it.copy(
                title = title,
                hasUnsavedChanges = true
            )
        }
    }

    private fun updateDescription(description: String) {
        _uiState.update {
            it.copy(
                description = description,
                hasUnsavedChanges = true
            )
        }
    }

    private fun updateKind(kind: PatternKind) {
        _uiState.update {
            it.copy(
                kind = kind,
                hasUnsavedChanges = true
            )
        }
    }

    private fun updateLoop(loop: Boolean) {
        _uiState.update {
            it.copy(
                loop = loop,
                hasUnsavedChanges = true
            )
        }
    }

    private fun showElementPicker() {
        viewModelScope.launch {
            _sideEffect.emit(PatternEditorSideEffect.ShowElementPicker)
        }
    }

    private fun addElement(element: PatternElement) {
        _uiState.update {
            it.copy(
                elements = it.elements + element,
                hasUnsavedChanges = true
            )
        }
    }

    private fun updateElement(index: Int, element: PatternElement) {
        _uiState.update {
            val updatedElements = it.elements.toMutableList()
            if (index in updatedElements.indices) {
                updatedElements[index] = element
            }
            it.copy(
                elements = updatedElements,
                hasUnsavedChanges = true
            )
        }
    }

    private fun removeElement(index: Int) {
        _uiState.update {
            val updatedElements = it.elements.toMutableList()
            if (index in updatedElements.indices) {
                updatedElements.removeAt(index)
            }
            it.copy(
                elements = updatedElements,
                selectedElementIndex = null,
                hasUnsavedChanges = true
            )
        }
    }

    private fun moveElement(from: Int, to: Int) {
        _uiState.update {
            val updatedElements = it.elements.toMutableList()
            if (from in updatedElements.indices && to in updatedElements.indices) {
                val element = updatedElements.removeAt(from)
                updatedElements.add(to, element)
            }
            it.copy(
                elements = updatedElements,
                hasUnsavedChanges = true
            )
        }
    }

    private fun selectElement(index: Int?) {
        _uiState.update { it.copy(selectedElementIndex = index) }
    }

    private fun savePattern() {
        val currentState = _uiState.value

        if (currentState.title.isBlank()) {
            _uiState.update {
                it.copy(validationErrors = mapOf("title" to "Введите название паттерна"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, validationErrors = emptyMap()) }

            val spec = PatternSpec(
                type = "custom",
                hardwareVersion = currentState.hardwareVersion,
                durationMs = null,
                loop = currentState.loop,
                elements = currentState.elements
            )

            if (currentState.isEditing && currentState.pattern != null) {
                // Обновление существующего паттерна
                val update = PatternUpdate(
                    title = currentState.title,
                    description = currentState.description.takeIf { it.isNotBlank() },
                    spec = spec
                )

                updatePatternUseCase(
                    PatternId(currentState.pattern.id.value),
                    currentState.pattern.version,
                    update
                ).onSuccess {
                    _uiState.update { it.copy(isSaving = false, hasUnsavedChanges = false) }
                    _sideEffect.emit(PatternEditorSideEffect.ShowSnackbar("Паттерн сохранён"))
                    _sideEffect.emit(PatternEditorSideEffect.NavigateBack)
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = error
                        )
                    }
                }
            } else {
                // Создание нового паттерна
                val draft = PatternDraft(
                    kind = currentState.kind,
                    spec = spec,
                    hardwareVersion = currentState.hardwareVersion,
                    title = currentState.title,
                    description = currentState.description.takeIf { it.isNotBlank() }
                )

                createPatternUseCase(draft)
                    .onSuccess {
                        _uiState.update { it.copy(isSaving = false, hasUnsavedChanges = false) }
                        _sideEffect.emit(PatternEditorSideEffect.ShowSnackbar("Паттерн создан"))
                        _sideEffect.emit(PatternEditorSideEffect.NavigateBack)
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                error = error
                            )
                        }
                    }
            }
        }
    }

    private fun publishPattern() {
        viewModelScope.launch {
            _sideEffect.emit(PatternEditorSideEffect.ShowPublishDialog)
        }
    }

    private fun previewPattern() {
        val currentState = _uiState.value
        val spec = PatternSpec(
            type = "custom",
            hardwareVersion = currentState.hardwareVersion,
            durationMs = null,
            loop = currentState.loop,
            elements = currentState.elements
        )

        viewModelScope.launch {
            _sideEffect.emit(PatternEditorSideEffect.NavigateToPreview(spec))
        }
    }

    private fun discardChanges() {
        if (_uiState.value.hasUnsavedChanges) {
            viewModelScope.launch {
                _sideEffect.emit(PatternEditorSideEffect.ShowDiscardConfirmation)
            }
        } else {
            viewModelScope.launch {
                _sideEffect.emit(PatternEditorSideEffect.NavigateBack)
            }
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
