package com.example.amulet.feature.practices.presentation.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.patterns.model.Pattern
import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.patterns.model.PatternWithSegments
import com.example.amulet.shared.domain.patterns.usecase.GetPresetsUseCase
import com.example.amulet.shared.domain.patterns.usecase.ObserveMyPatternsUseCase
import com.example.amulet.shared.domain.patterns.usecase.PatternEditorFacade
import com.example.amulet.shared.domain.practices.model.Practice
import com.example.amulet.shared.domain.practices.model.PracticeId
import com.example.amulet.shared.domain.practices.model.PracticeScript
import com.example.amulet.shared.domain.practices.model.PracticeStep
import com.example.amulet.shared.domain.practices.model.PracticeStepType
import com.example.amulet.shared.domain.practices.model.PracticeType
import com.example.amulet.shared.domain.practices.usecase.GetPracticeByIdUseCase
import com.example.amulet.shared.domain.practices.usecase.UpsertPracticeUseCase
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PracticeEditorViewModel @Inject constructor(
    private val getPracticeByIdUseCase: GetPracticeByIdUseCase,
    private val upsertPracticeUseCase: UpsertPracticeUseCase,
    private val patternEditorFacade: PatternEditorFacade,
    private val observeMyPatternsUseCase: ObserveMyPatternsUseCase,
    private val getPresetsUseCase: GetPresetsUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val practiceIdArg: String? = savedStateHandle.get<String>("practiceId")

    private val _state = MutableStateFlow(
        PracticeEditorState(
            isLoading = practiceIdArg != null,
            practiceId = practiceIdArg,
        )
    )
    val state: StateFlow<PracticeEditorState> = _state.asStateFlow()

    private val _effect = Channel<PracticeEditorEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var loadedPractice: Practice? = null
    private var patternJob: Job? = null
    private var patternsJob: Job? = null

    init {
        observeAvailablePatterns()
        if (practiceIdArg != null) {
            loadPractice(practiceIdArg)
        } else {
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun handleEvent(event: PracticeEditorEvent) {
        when (event) {
            PracticeEditorEvent.InitIfNeeded -> {
            }
            is PracticeEditorEvent.UpdateTitle -> updateTitle(event.title)
            is PracticeEditorEvent.UpdateType -> updateType(event.type)
            is PracticeEditorEvent.UpdateTargetDuration -> updateTargetDuration(event.durationSec)
            is PracticeEditorEvent.UpdateAbout -> updateAbout(event.text)
            is PracticeEditorEvent.UpdateHowItGoes -> updateHowItGoes(event.text)
            is PracticeEditorEvent.UpdateSafetyNotes -> updateSafetyNotes(event.text)
            PracticeEditorEvent.TogglePatternSheet -> togglePatternSheet()
            is PracticeEditorEvent.SetBasePattern -> setBasePattern(event.patternId)
            PracticeEditorEvent.GenerateStepsFromSegments -> generateStepsFromSegments()
            is PracticeEditorEvent.UpdateStepTitle -> updateStepTitle(event.order, event.title)
            is PracticeEditorEvent.UpdateStepDescription -> updateStepDescription(event.order, event.description)
            is PracticeEditorEvent.UpdateStepDuration -> updateStepDuration(event.order, event.durationSec)
            is PracticeEditorEvent.UpdateSegmentGroupRepeat -> updateSegmentGroupRepeat(event.order, event.repeatCount)
            is PracticeEditorEvent.MergeStepWithNext -> mergeStepWithNext(event.order)
            is PracticeEditorEvent.ToggleSegmentInStep -> toggleSegmentInStep(event.order, event.segmentIndex)
            PracticeEditorEvent.AddStep -> addStep()
            is PracticeEditorEvent.DeleteStep -> deleteStep(event.order)
            is PracticeEditorEvent.SelectStep -> selectStep(event.order)
            PracticeEditorEvent.Save -> save()
            PracticeEditorEvent.DismissError -> dismissError()
            PracticeEditorEvent.OnBackClick -> navigateBack()
        }
    }

    private fun loadPractice(id: PracticeId) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val practice = getPracticeByIdUseCase(id).first()
            if (practice == null) {
                _state.update { it.copy(isLoading = false, error = AppError.NotFound) }
                return@launch
            }
            loadedPractice = practice
            val editor = practice.toEditor()
            _state.update {
                it.copy(
                    isLoading = false,
                    practiceId = practice.id,
                    editorPractice = editor,
                )
            }
            editor.basePatternId?.let { patternId ->
                observePatternWithSegments(patternId)
            }
        }
    }

    private fun observeAvailablePatterns() {
        patternsJob?.cancel()
        patternsJob = viewModelScope.launch {
            combine(
                observeMyPatternsUseCase(),
                getPresetsUseCase(),
            ) { my, presets ->
                (my + presets).distinctBy { it.id }
            }.collect { patterns: List<Pattern> ->
                _state.update { it.copy(availablePatterns = patterns) }
            }
        }
    }

    private fun observePatternWithSegments(patternId: PatternId) {
        patternJob?.cancel()
        patternJob = viewModelScope.launch {
            _state.update { it.copy(basePatternLoading = true) }
            patternEditorFacade.getPatternWithSegments(patternId).collect { pws ->
                if (pws != null) {
                    autoGenerateStepsFromSegmentsIfEmpty(pws)
                }
                _state.update {
                    it.copy(
                        basePatternWithSegments = pws,
                        basePatternLoading = false,
                    )
                }
            }
        }
    }

    private fun updateTitle(title: String) {
        _state.update { state ->
            state.copy(editorPractice = state.editorPractice.copy(title = title))
        }
    }

    private fun updateType(type: PracticeType) {
        _state.update { state ->
            state.copy(editorPractice = state.editorPractice.copy(type = type))
        }
    }

    private fun updateTargetDuration(durationSec: Int?) {
        _state.update { state ->
            state.copy(editorPractice = state.editorPractice.copy(targetDurationSec = durationSec))
        }
    }

    private fun updateAbout(text: String) {
        _state.update { state ->
            state.copy(editorPractice = state.editorPractice.copy(about = text))
        }
    }

    private fun updateHowItGoes(text: String) {
        _state.update { state ->
            state.copy(editorPractice = state.editorPractice.copy(howItGoes = text))
        }
    }

    private fun updateSafetyNotes(text: String) {
        _state.update { state ->
            state.copy(editorPractice = state.editorPractice.copy(safetyNotes = text))
        }
    }

    private fun setBasePattern(patternId: PatternId) {
        _state.update { state ->
            state.copy(
                editorPractice = state.editorPractice.copy(basePatternId = patternId),
                isPatternSheetVisible = false,
            )
        }
        observePatternWithSegments(patternId)
    }

    private fun generateStepsFromSegments() {
        val current = _state.value
        val data: PatternWithSegments = current.basePatternWithSegments ?: run {
            viewModelScope.launch {
                _effect.send(PracticeEditorEffect.ShowMessage("Сначала выберите паттерн с сегментами"))
            }
            return
        }

        if (!autoGenerateStepsFromSegmentsInternal(current, data)) {
            viewModelScope.launch {
                _effect.send(PracticeEditorEffect.ShowMessage("У паттерна нет сегментов"))
            }
        }
    }

    private fun autoGenerateStepsFromSegmentsIfEmpty(data: PatternWithSegments) {
        val current = _state.value
        if (current.editorPractice.steps.isNotEmpty()) return
        autoGenerateStepsFromSegmentsInternal(current, data)
    }

    private fun autoGenerateStepsFromSegmentsInternal(
        current: PracticeEditorState,
        data: PatternWithSegments,
    ): Boolean {
        val basePattern = data.base
        val segments = data.segments
        if (segments.isEmpty()) {
            return false
        }

        val stepType = when (current.editorPractice.type) {
            PracticeType.BREATH -> PracticeStepType.BREATH_STEP
            PracticeType.MEDITATION -> PracticeStepType.TEXT_HINT
            PracticeType.SOUND -> PracticeStepType.SOUND_SCAPE
        }

        val editorSteps = segments.sortedBy { it.segmentIndex ?: 0 }.mapIndexed { index, segment ->
            val segIndex = segment.segmentIndex ?: index
            EditorStep(
                order = index,
                type = stepType,
                title = "",
                description = "",
                durationSec = (segment.spec.timeline.durationMs / 1000).takeIf { it > 0 },
                binding = StepBinding.SegmentGroup(
                    parentPatternId = basePattern.id,
                    segmentIndices = listOf(segIndex),
                    repeatCount = 1,
                )
            )
        }

        val limitedSteps = editorSteps.take(MAX_STEPS)
        if (editorSteps.size > MAX_STEPS) {
            viewModelScope.launch {
                _effect.send(PracticeEditorEffect.ShowMessage("Максимальное число шагов: $MAX_STEPS. Лишние сегменты были проигнорированы."))
            }
        }

        _state.update {
            it.copy(
                editorPractice = it.editorPractice.copy(steps = limitedSteps),
                selectedStepOrder = limitedSteps.firstOrNull()?.order,
            )
        }

        return segments.isNotEmpty()
    }

    private fun updateStepTitle(order: Int, title: String) {
        _state.update { state ->
            state.copy(
                editorPractice = state.editorPractice.copy(
                    steps = state.editorPractice.steps.map { step ->
                        if (step.order == order) step.copy(title = title) else step
                    }
                )
            )
        }
    }

    private fun updateStepDescription(order: Int, description: String) {
        _state.update { state ->
            state.copy(
                editorPractice = state.editorPractice.copy(
                    steps = state.editorPractice.steps.map { step ->
                        if (step.order == order) step.copy(description = description) else step
                    }
                )
            )
        }
    }

    private fun updateStepDuration(order: Int, durationSec: Int?) {
        _state.update { state ->
            state.copy(
                editorPractice = state.editorPractice.copy(
                    steps = state.editorPractice.steps.map { step ->
                        if (step.order == order) step.copy(durationSec = durationSec) else step
                    }
                )
            )
        }
    }

    private fun updateSegmentGroupRepeat(order: Int, repeatCount: Int) {
        _state.update { state ->
            state.copy(
                editorPractice = state.editorPractice.copy(
                    steps = state.editorPractice.steps.map { step ->
                        if (step.order == order) {
                            val binding = step.binding
                            if (binding is StepBinding.SegmentGroup) {
                                val value = repeatCount.coerceAtLeast(1).coerceAtMost(MAX_REPEAT_COUNT)
                                step.copy(binding = binding.copy(repeatCount = value))
                            } else {
                                step
                            }
                        } else {
                            step
                        }
                    }
                )
            )
        }
    }

    private fun mergeStepWithNext(order: Int) {
        val current = _state.value
        val sorted = current.editorPractice.steps.sortedBy { it.order }.toMutableList()
        val idx = sorted.indexOfFirst { it.order == order }
        if (idx == -1 || idx >= sorted.lastIndex) return
        val first = sorted[idx]
        val second = sorted[idx + 1]
        val firstBinding = first.binding
        val secondBinding = second.binding
        if (firstBinding is StepBinding.SegmentGroup && secondBinding is StepBinding.SegmentGroup && firstBinding.parentPatternId == secondBinding.parentPatternId) {
            val merged = firstBinding.segmentIndices + secondBinding.segmentIndices
            val newStep = first.copy(
                binding = firstBinding.copy(
                    segmentIndices = merged.distinct().sorted(),
                    repeatCount = firstBinding.repeatCount,
                )
            )
            sorted[idx] = newStep
            sorted.removeAt(idx + 1)
            val reindexed = sorted.mapIndexed { index, step -> step.copy(order = index) }
            _state.update {
                it.copy(editorPractice = it.editorPractice.copy(steps = reindexed))
            }
        }
    }

    private fun toggleSegmentInStep(order: Int, segmentIndex: Int) {
        _state.update { state ->
            val updatedSteps = state.editorPractice.steps.map { step ->
                if (step.order != order) return@map step
                val binding = step.binding
                if (binding is StepBinding.SegmentGroup) {
                    val has = binding.segmentIndices.contains(segmentIndex)
                    val newIndices = if (has) {
                        binding.segmentIndices.filterNot { it == segmentIndex }
                    } else {
                        (binding.segmentIndices + segmentIndex).distinct().sorted()
                    }
                    if (newIndices.isEmpty()) {
                        step.copy(binding = StepBinding.None)
                    } else {
                        step.copy(binding = binding.copy(segmentIndices = newIndices))
                    }
                } else {
                    step
                }
            }
            state.copy(editorPractice = state.editorPractice.copy(steps = updatedSteps))
        }
    }

    private fun addStep() {
        val currentSteps = state.value.editorPractice.steps.sortedBy { it.order }.toMutableList()
        if (currentSteps.size >= MAX_STEPS) {
            viewModelScope.launch {
                _effect.send(PracticeEditorEffect.ShowMessage("Достигнут лимит по количеству шагов: $MAX_STEPS"))
            }
            return
        }
        val newStep = EditorStep(
            order = currentSteps.size,
            type = PracticeStepType.TEXT_HINT,
            title = "",
            description = "",
            durationSec = null,
            binding = StepBinding.None,
        )
        currentSteps.add(newStep)
        val reindexed = currentSteps.mapIndexed { index, step -> step.copy(order = index) }
        _state.update {
            it.copy(editorPractice = it.editorPractice.copy(steps = reindexed))
        }
    }

    private fun deleteStep(order: Int) {
        val currentSteps = state.value.editorPractice.steps.filterNot { it.order == order }.sortedBy { it.order }
        val reindexed = currentSteps.mapIndexed { index, step -> step.copy(order = index) }
        _state.update {
            it.copy(editorPractice = it.editorPractice.copy(steps = reindexed))
        }
    }

    private fun selectStep(order: Int?) {
        _state.update { it.copy(selectedStepOrder = order) }
    }

    private fun togglePatternSheet() {
        _state.update { it.copy(isPatternSheetVisible = !it.isPatternSheetVisible) }
    }

    private fun save() {
        val current = _state.value
        val editor = current.editorPractice
        if (editor.title.isBlank()) {
            _state.update { it.copy(error = AppError.Validation(mapOf("title" to "Введите название практики"))) }
            return
        }
        val steps = editor.steps.sortedBy { it.order }
        val hasPatternStep = steps.any { step ->
            when (val b = step.binding) {
                is StepBinding.SinglePattern -> true
                is StepBinding.SegmentGroup -> b.segmentIndices.isNotEmpty()
                else -> false
            }
        }
        val hasTextStep = steps.any { step ->
            step.binding is StepBinding.None &&
                (step.title.isNotBlank() || step.description.isNotBlank() || (step.durationSec ?: 0) > 0)
        }
        if (!hasPatternStep && !hasTextStep) {
            _state.update { it.copy(error = AppError.Validation(mapOf("steps" to "Добавьте хотя бы один шаг практики"))) }
            return
        }

        val practice = buildPractice(
            editor = editor,
            patternWithSegments = current.basePatternWithSegments,
            availablePatterns = current.availablePatterns,
        )
        val totalDuration = practice.durationSec ?: 0
        if (totalDuration <= 0) {
            _state.update { it.copy(error = AppError.Validation(mapOf("duration" to "Суммарная длительность практики должна быть больше 0"))) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            upsertPracticeUseCase(practice)
                .onSuccess {
                    loadedPractice = practice
                    _state.update { state ->
                        state.copy(
                            isSaving = false,
                            practiceId = practice.id,
                            error = null,
                        )
                    }
                    _effect.send(PracticeEditorEffect.NavigateBack)
                }
                .onFailure { error ->
                    _state.update { state ->
                        state.copy(isSaving = false, error = error)
                    }
                    _effect.send(PracticeEditorEffect.ShowMessage("Не удалось сохранить практику"))
                }
        }
    }

    private fun buildPractice(
        editor: EditorPractice,
        patternWithSegments: PatternWithSegments?,
        availablePatterns: List<Pattern>,
    ): Practice {
        val base = loadedPractice
        val now = System.currentTimeMillis()
        val id: PracticeId = base?.id ?: editor.id ?: UUID.randomUUID().toString()

        val patternsById: Map<PatternId, Pattern> = availablePatterns.associateBy { it.id }
        val script = buildScript(editor, patternWithSegments, patternsById)
        val scriptSteps = script.steps

        // Продолжительность практики: явное значение пользователя имеет приоритет,
        // иначе суммируем продолжительности всех шагов скрипта, при отсутствии — берём из base.
        val scriptDurationSec: Int? = scriptSteps
            .mapNotNull { it.durationSec }
            .takeIf { it.isNotEmpty() }
            ?.sum()

        val isScriptEmpty = scriptSteps.isEmpty()

        val finalDurationSec: Int? = editor.targetDurationSec
            ?: scriptDurationSec
            ?: base?.durationSec

        val isPatternOnlyPractice = isScriptEmpty && editor.basePatternId != null

        val hasDeviceScriptFlag = !isPatternOnlyPractice && scriptSteps.any { it.patternId != null }

        val finalPatternId = when {
            isPatternOnlyPractice -> editor.basePatternId
            else -> editor.basePatternId ?: base?.patternId
        }

        val finalScript = when {
            isPatternOnlyPractice -> null
            else -> script
        }

        val editedSteps: List<String> = editor.howItGoes
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val finalSteps: List<String> = if (editedSteps.isNotEmpty()) {
            editedSteps
        } else {
            base?.steps ?: emptyList()
        }

        val editedSafetyNotes: List<String> = editor.safetyNotes
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val finalSafetyNotes: List<String> = if (editedSafetyNotes.isNotEmpty()) {
            editedSafetyNotes
        } else {
            base?.safetyNotes ?: emptyList()
        }

        return Practice(
            id = id,
            type = editor.type,
            title = editor.title,
            description = editor.about.ifBlank { base?.description },
            durationSec = finalDurationSec,
            level = base?.level,
            goal = base?.goal,
            tags = base?.tags ?: emptyList(),
            contraindications = base?.contraindications ?: emptyList(),
            patternId = finalPatternId,
            audioUrl = base?.audioUrl,
            isFavorite = base?.isFavorite ?: false,
            usageCount = base?.usageCount ?: 0,
            createdAt = base?.createdAt ?: now,
            updatedAt = now,
            steps = finalSteps,
            safetyNotes = finalSafetyNotes,
            script = finalScript,
            hasDeviceScript = hasDeviceScriptFlag,
        )
    }

    private fun buildScript(
        editor: EditorPractice,
        patternWithSegments: PatternWithSegments?,
        patternsById: Map<PatternId, Pattern>,
    ): PracticeScript {
        val steps = mutableListOf<PracticeStep>()
        var order = 0

        val baseSegments = patternWithSegments
        val segmentsByIndex = baseSegments?.segments?.associateBy { it.segmentIndex ?: 0 } ?: emptyMap()

        editor.steps.sortedBy { it.order }.forEach { step ->
            if (steps.size >= MAX_SCRIPT_STEPS) return@forEach
            when (val binding = step.binding) {
                StepBinding.None -> {
                    steps += PracticeStep(
                        order = order++,
                        type = step.type,
                        title = step.title.ifBlank { null },
                        description = step.description.ifBlank { null },
                        durationSec = step.durationSec,
                        patternId = null,
                        audioUrl = null,
                        extra = emptyMap(),
                    )
                }
                is StepBinding.SinglePattern -> {
                    val repeat = binding.repeatCount.coerceAtLeast(1).coerceAtMost(MAX_REPEAT_COUNT)
                    val pattern = patternsById[binding.patternId]
                    val fromPatternDurationSec = pattern?.spec?.timeline?.durationMs
                        ?.let { (it / 1000).coerceAtLeast(1) }
                    val durationSec = step.durationSec ?: fromPatternDurationSec

                    repeat(repeat) {
                        steps += PracticeStep(
                            order = order++,
                            type = step.type,
                            title = step.title.ifBlank { null },
                            description = step.description.ifBlank { null },
                            durationSec = durationSec,
                            patternId = binding.patternId.value,
                            audioUrl = null,
                            extra = emptyMap(),
                        )
                    }
                }
                is StepBinding.SegmentGroup -> {
                    val repeat = binding.repeatCount.coerceAtLeast(1).coerceAtMost(MAX_REPEAT_COUNT)
                    val indices = binding.segmentIndices.distinct().sorted()
                    if (indices.isEmpty() || segmentsByIndex.isEmpty()) {
                        steps += PracticeStep(
                            order = order++,
                            type = step.type,
                            title = step.title.ifBlank { null },
                            description = step.description.ifBlank { null },
                            durationSec = step.durationSec,
                            patternId = null,
                            audioUrl = null,
                            extra = emptyMap(),
                        )
                    } else {
                        repeat(repeat) {
                            indices.forEach { idx ->
                                if (steps.size >= MAX_SCRIPT_STEPS) return@repeat
                                val segment = segmentsByIndex[idx]
                                if (segment != null) {
                                    val segmentDurationSec = (segment.spec.timeline.durationMs / 1000)
                                        .coerceAtLeast(1)
                                    val durationSec = step.durationSec ?: segmentDurationSec

                                    steps += PracticeStep(
                                        order = order++,
                                        type = step.type,
                                        title = step.title.ifBlank { null },
                                        description = step.description.ifBlank { null },
                                        durationSec = durationSec,
                                        patternId = segment.id.value,
                                        audioUrl = null,
                                        extra = mapOf(
                                            "segmentParentPatternId" to binding.parentPatternId.value,
                                            "segmentIndex" to idx.toString(),
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        return PracticeScript(steps = steps)
    }

    private fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _effect.send(PracticeEditorEffect.NavigateBack)
        }
    }

    private fun Practice.toEditor(): EditorPractice {
        val scriptSteps = script?.steps?.sortedBy { it.order }.orEmpty()
        val editorSteps = if (scriptSteps.isNotEmpty()) {
            scriptSteps.mapIndexed { index, step ->
                val binding = step.patternId
                    ?.let { id -> StepBinding.SinglePattern(patternId = PatternId(id), repeatCount = 1) }
                    ?: StepBinding.None
                EditorStep(
                    order = index,
                    type = step.type,
                    title = step.title ?: "",
                    description = step.description ?: "",
                    durationSec = step.durationSec,
                    binding = binding,
                )
            }
        } else {
            emptyList()
        }

        return EditorPractice(
            id = id,
            title = title,
            type = type,
            targetDurationSec = durationSec,
            basePatternId = patternId,
            about = description.orEmpty(),
            howItGoes = steps.joinToString(separator = "\n"),
            safetyNotes = safetyNotes.joinToString(separator = "\n"),
            steps = editorSteps,
        )
    }

    private companion object {
        private const val MAX_STEPS: Int = 64
        private const val MAX_SCRIPT_STEPS: Int = 512
        private const val MAX_REPEAT_COUNT: Int = 16
    }
}
