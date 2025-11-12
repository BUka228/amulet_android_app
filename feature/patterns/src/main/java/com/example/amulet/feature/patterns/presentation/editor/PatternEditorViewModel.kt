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
        // Автоматически обновляем spec при изменении элементов
        viewModelScope.launch {
            _uiState.map { it.elements }.distinctUntilChanged().collect {
                updateSpec()
            }
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
            is PatternEditorEvent.ConfirmPublish -> confirmPublish(event.data)
            is PatternEditorEvent.DiscardChanges -> discardChanges()
            is PatternEditorEvent.ConfirmDiscard -> confirmDiscard()
            is PatternEditorEvent.DismissError -> dismissError()
            is PatternEditorEvent.TogglePreviewPlayback -> togglePreviewPlayback()
            is PatternEditorEvent.RestartPreview -> restartPreview()
            is PatternEditorEvent.TogglePreviewExpanded -> togglePreviewExpanded()
            is PatternEditorEvent.SendToDevice -> sendToDevice()
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
                                isEditing = true,
                                spec = it.spec
                            )
                        }
                        updateSpec()
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
        updateSpec()
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
        updateSpec()
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
        updateSpec()
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
        updateSpec()
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
        updateSpec()
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

    private fun confirmPublish(data: com.example.amulet.feature.patterns.presentation.components.PublishPatternData) {
        val currentState = _uiState.value
        
        if (currentState.pattern == null) {
            viewModelScope.launch {
                _sideEffect.emit(PatternEditorSideEffect.ShowSnackbar("Сначала сохраните паттерн"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val metadata = PublishMetadata(
                title = data.publicTitle,
                description = data.publicDescription,
                tags = data.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            )

            publishPatternUseCase(
                id = currentState.pattern.id,
                metadata = metadata
            ).onSuccess {
                _uiState.update { it.copy(isSaving = false) }
                _sideEffect.emit(PatternEditorSideEffect.ShowSnackbar("Паттерн опубликован"))
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = error
                    )
                }
            }
        }
    }

    private fun updateSpec() {
        val currentState = _uiState.value
        val spec = if (currentState.elements.isNotEmpty()) {
            PatternSpec(
                type = "custom",
                hardwareVersion = currentState.hardwareVersion,
                durationMs = null,
                loop = currentState.loop,
                elements = currentState.elements
            )
        } else {
            null
        }
        _uiState.update { it.copy(spec = spec) }
    }

    private fun togglePreviewPlayback() {
        _uiState.update { it.copy(isPreviewPlaying = !it.isPreviewPlaying) }
    }

    private fun restartPreview() {
        _uiState.update { 
            it.copy(
                isPreviewPlaying = false
            )
        }
        // Небольшая задержка для рестарта анимации
        viewModelScope.launch {
            kotlinx.coroutines.delay(50)
            _uiState.update { it.copy(isPreviewPlaying = true) }
        }
    }

    private fun togglePreviewExpanded() {
        _uiState.update { it.copy(isPreviewExpanded = !it.isPreviewExpanded) }
    }

    private fun sendToDevice() {
        val currentState = _uiState.value
        
        if (currentState.spec == null) {
            viewModelScope.launch {
                _sideEffect.emit(PatternEditorSideEffect.ShowSnackbar("Добавьте элементы паттерна"))
            }
            return
        }

        viewModelScope.launch {
            _sideEffect.emit(PatternEditorSideEffect.NavigateToPreview(currentState.spec))
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

    private fun confirmDiscard() {
        viewModelScope.launch {
            _sideEffect.emit(PatternEditorSideEffect.NavigateBack)
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
