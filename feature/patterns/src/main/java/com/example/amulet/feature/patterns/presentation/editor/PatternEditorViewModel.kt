package com.example.amulet.feature.patterns.presentation.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.domain.patterns.model.PatternDraft
import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.patterns.model.PatternKind
import com.example.amulet.shared.domain.patterns.model.PatternSpec
import com.example.amulet.shared.domain.patterns.model.PatternTimeline
import com.example.amulet.shared.domain.patterns.model.PatternUpdate
import com.example.amulet.shared.domain.patterns.model.PublishMetadata
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
    private val getAllTagsUseCase: GetAllTagsUseCase,
    private val createTagsUseCase: CreateTagsUseCase,
    private val setPatternTagsUseCase: SetPatternTagsUseCase,
    private val deleteTagsUseCase: DeleteTagsUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val patternId: String? = savedStateHandle.get<String>("patternId")

    private val _uiState = MutableStateFlow(
        PatternEditorState(
            patternId = patternId,
            timeline = DEFAULT_TIMELINE,
            spec = defaultSpec(DEFAULT_TIMELINE)
        )
    )
    val uiState: StateFlow<PatternEditorState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<PatternEditorSideEffect>()
    val sideEffect: SharedFlow<PatternEditorSideEffect> = _sideEffect.asSharedFlow()

    init {
        if (patternId != null) {
            loadPattern()
        }
        // Загрузим справочник тегов
        viewModelScope.launch {
            getAllTagsUseCase().onSuccess { tags ->
                _uiState.update { it.copy(availableTags = tags.sorted()) }
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
            is PatternEditorEvent.TogglePlayPause -> togglePlayPause()
            is PatternEditorEvent.ToggleLoop -> toggleLoop()
            is PatternEditorEvent.RestartPreview -> restartPreview()
            is PatternEditorEvent.UpdateTimeline -> updateTimeline(event.timeline)
            is PatternEditorEvent.SavePattern -> savePattern()
            is PatternEditorEvent.PublishPattern -> publishPattern()
            is PatternEditorEvent.ConfirmPublish -> confirmPublish(event.data)
            is PatternEditorEvent.DiscardChanges -> discardChanges()
            is PatternEditorEvent.ConfirmDiscard -> confirmDiscard()
            is PatternEditorEvent.DismissError -> dismissError()
            is PatternEditorEvent.SendToDevice -> sendToDevice()
            is PatternEditorEvent.ShowTagsSheet -> _uiState.update { it.copy(showTagsSheet = true) }
            is PatternEditorEvent.HideTagsSheet -> _uiState.update { it.copy(showTagsSheet = false) }
            is PatternEditorEvent.UpdateTagSearch -> _uiState.update { it.copy(tagSearchQuery = event.query) }
            is PatternEditorEvent.ToggleTag -> toggleTag(event.tag)
            is PatternEditorEvent.AddNewTag -> addNewTag(event.tag)
            is PatternEditorEvent.DeleteSelectedTags -> deleteSelectedTags()
            is PatternEditorEvent.SetPendingDeleteTags -> _uiState.update { it.copy(pendingDeleteTags = event.tags) }
        }
    }

    private fun loadPattern() {
        if (patternId == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getPatternByIdUseCase(PatternId(patternId))
                .collect { pattern ->
                    pattern?.let {
                        val tl = it.spec.timeline
                        _uiState.update { state ->
                            state.copy(
                                pattern = it,
                                title = it.title,
                                description = it.description ?: "",
                                kind = it.kind,
                                hardwareVersion = it.hardwareVersion,
                                loop = it.spec.loop,
                                timeline = tl,
                                isLoading = false,
                                isEditing = true,
                                spec = it.spec,
                                selectedTags = it.tags.toSet()
                            )
                        }
                    } ?: run {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
        }
    }

    private fun toggleTag(tag: String) {
        val current = _uiState.value
        val newSet = if (current.selectedTags.contains(tag)) {
            current.selectedTags - tag
        } else {
            current.selectedTags + tag
        }
        // Меняем только локальное состояние, сохраняем связи при сохранении паттерна
        _uiState.update {
            it.copy(
                selectedTags = newSet,
                availableTags = (it.availableTags + tag).distinct().sorted(),
                hasUnsavedChanges = true
            )
        }
    }

    private fun addNewTag(tag: String) {
        val trimmed = tag.trim()
        if (trimmed.isEmpty()) return
        val current = _uiState.value
        viewModelScope.launch {
            // 1) Создаём тег в БД (если уже есть — игнорируется)
            createTagsUseCase(listOf(trimmed))
                .onSuccess {
                    val updatedAvailable = (current.availableTags + trimmed).distinct().sorted()
                    val newSet = current.selectedTags + trimmed
                    _uiState.update { it.copy(selectedTags = newSet, availableTags = updatedAvailable, hasUnsavedChanges = true) }
                }
        }
    }

    private fun deleteSelectedTags() {
        val tagsToDelete = _uiState.value.pendingDeleteTags
        if (tagsToDelete.isEmpty()) return
        viewModelScope.launch {
            deleteTagsUseCase(tagsToDelete.toList())
                .onSuccess {
                    // обновить доступные и выбранные
                    val newAvailable = _uiState.value.availableTags.filterNot { it in tagsToDelete }
                    val newSelected = _uiState.value.selectedTags - tagsToDelete
                    _uiState.update { it.copy(availableTags = newAvailable, selectedTags = newSelected, pendingDeleteTags = emptySet()) }
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
    
    private fun togglePlayPause() {
        _uiState.update {
            it.copy(
                isPlaying = !it.isPlaying
            )
        }
    }

    private fun toggleLoop() {
        _uiState.update {
            it.copy(
                previewLoop = !it.previewLoop
            )
        }
    }
    
    private fun restartPreview() {
        // Перезапуск анимации путем переключения состояния
        _uiState.update {
            it.copy(
                isPlaying = false
            )
        }
        
        // Немедленно включаем воспроизведение
        _uiState.update {
            it.copy(
                isPlaying = true
            )
        }
    }

    private fun updateTimeline(timeline: PatternTimeline) {
        _uiState.update {
            it.copy(
                timeline = timeline,
                hasUnsavedChanges = true
            )
        }
        updateSpec()
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

            val timeline = currentState.timeline ?: DEFAULT_TIMELINE
            val spec = PatternSpec(
                type = "custom",
                hardwareVersion = currentState.hardwareVersion,
                durationMs = null,
                loop = currentState.loop,
                timeline = timeline
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
                    // применяем связи тегов
                    setPatternTagsUseCase(currentState.pattern.id, currentState.selectedTags.toList())
                        .onSuccess {
                            _uiState.update { it.copy(isSaving = false, hasUnsavedChanges = false) }
                            _sideEffect.emit(PatternEditorSideEffect.ShowSnackbar("Паттерн сохранён"))
                            _sideEffect.emit(PatternEditorSideEffect.NavigateBack)
                        }
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
                    .onSuccess { created ->
                        // применяем связи тегов для нового паттерна
                        setPatternTagsUseCase(created.id, currentState.selectedTags.toList())
                            .onSuccess {
                                _uiState.update { it.copy(isSaving = false, hasUnsavedChanges = false) }
                                _sideEffect.emit(PatternEditorSideEffect.ShowSnackbar("Паттерн создан"))
                                _sideEffect.emit(PatternEditorSideEffect.NavigateBack)
                            }
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
        val timeline = currentState.timeline
        val spec = timeline?.let {
            PatternSpec(
                type = "custom",
                hardwareVersion = currentState.hardwareVersion,
                durationMs = null,
                loop = currentState.loop,
                timeline = it
            )
        }
        _uiState.update { it.copy(spec = spec) }
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

    private companion object {
        val DEFAULT_TIMELINE = PatternTimeline(
            durationMs = 3000,
            tracks = emptyList()
        )

        fun defaultSpec(timeline: PatternTimeline) = PatternSpec(
            type = "custom",
            hardwareVersion = 100,
            durationMs = null,
            loop = false,
            timeline = timeline
        )
    }
}
