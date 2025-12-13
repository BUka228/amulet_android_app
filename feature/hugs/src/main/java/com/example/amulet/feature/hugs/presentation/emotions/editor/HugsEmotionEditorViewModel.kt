package com.example.amulet.feature.hugs.presentation.emotions.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.domain.hugs.ObservePairEmotionsUseCase
import com.example.amulet.shared.domain.hugs.ObservePairsUseCase
import com.example.amulet.shared.domain.hugs.UpdatePairEmotionsUseCase
import com.example.amulet.shared.domain.hugs.model.PairEmotion
import com.example.amulet.shared.domain.hugs.model.PairId
import com.example.amulet.shared.domain.hugs.model.PairStatus
import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.patterns.usecase.GetPatternByIdUseCase
import com.example.amulet.shared.domain.user.usecase.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.ExperimentalCoroutinesApi

@HiltViewModel
class HugsEmotionEditorViewModel @Inject constructor(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val observePairsUseCase: ObservePairsUseCase,
    private val observePairEmotionsUseCase: ObservePairEmotionsUseCase,
    private val updatePairEmotionsUseCase: UpdatePairEmotionsUseCase,
    private val getPatternByIdUseCase: GetPatternByIdUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(HugsEmotionEditorState())
    val state: StateFlow<HugsEmotionEditorState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<HugsEmotionEditorEffect>()
    val effects = _effects.asSharedFlow()

    private val emotionIdArg: String? = savedStateHandle.get<String>("emotionId")?.takeIf { it.isNotBlank() }

    private var activePairId: String? = null
    private var currentEmotions: List<PairEmotion> = emptyList()
    private var patternJob: Job? = null

    init {
        _state.update { it.copy(emotionId = emotionIdArg) }
        observeData()
    }

    fun onIntent(intent: HugsEmotionEditorIntent) {
        when (intent) {
            is HugsEmotionEditorIntent.ChangeName -> changeName(intent.value)
            is HugsEmotionEditorIntent.ChangeColor -> changeColor(intent.colorHex)
            is HugsEmotionEditorIntent.ChangePattern -> changePattern(intent.patternId)
            HugsEmotionEditorIntent.OpenPatternPicker -> openPatternPicker()
            HugsEmotionEditorIntent.Save -> save()
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val userFlow = observeCurrentUserUseCase()
            val pairsFlow = observePairsUseCase()

            userFlow
                .combine(pairsFlow) { _, pairs ->
                    val activePair = pairs.firstOrNull { it.status == PairStatus.ACTIVE } ?: pairs.firstOrNull()
                    activePair
                }
                .flatMapLatest { pair ->
                    if (pair == null) {
                        flowOf(PairData(pairId = null, emotions = emptyList()))
                    } else {
                        observePairEmotionsUseCase(pair.id)
                            .map { emotions -> PairData(pairId = pair.id.value, emotions = emotions) }
                    }
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = PairData(pairId = null, emotions = emptyList())
                )
                .collect { data ->
                    activePairId = data.pairId
                    currentEmotions = data.emotions

                    val emotion = resolveEmotion(
                        emotionId = emotionIdArg,
                        pairId = data.pairId,
                        emotions = data.emotions,
                    )

                    observePatternTitle(emotion?.patternId)

                    _state.update {
                        it.copy(
                            isLoading = false,
                            emotion = emotion,
                        )
                    }
                }
        }
    }

    private fun resolveEmotion(
        emotionId: String?,
        pairId: String?,
        emotions: List<PairEmotion>,
    ): PairEmotion? {
        if (pairId == null) return null

        val existing = emotionId?.let { id -> emotions.firstOrNull { it.id == id } }
        if (existing != null) return existing

        if (emotionId != null) return null

        val nextOrder = (emotions.maxOfOrNull { it.order } ?: 0) + 1
        val palette = listOf(
            "#FF4D4D",
            "#FF8A3D",
            "#FFD43D",
            "#2ECC71",
            "#4D9DFF",
            "#9B59B6",
        )
        val colorHex = palette[(nextOrder - 1).mod(palette.size)]

        return PairEmotion(
            id = UUID.randomUUID().toString(),
            pairId = PairId(pairId),
            name = "",
            colorHex = colorHex,
            patternId = null,
            order = nextOrder,
        )
    }

    private fun changeName(value: String) {
        val current = _state.value.emotion ?: return
        _state.update { it.copy(emotion = current.copy(name = value)) }
    }

    private fun changeColor(colorHex: String) {
        val current = _state.value.emotion ?: return
        _state.update { it.copy(emotion = current.copy(colorHex = colorHex)) }
    }

    private fun openPatternPicker() {
        viewModelScope.launch {
            _effects.emit(HugsEmotionEditorEffect.OpenPatternPicker)
        }
    }

    private fun changePattern(patternId: String?) {
        val current = _state.value.emotion ?: return
        val mapped = patternId?.takeIf { it.isNotBlank() }?.let { PatternId(it) }
        _state.update { it.copy(emotion = current.copy(patternId = mapped)) }
        observePatternTitle(mapped)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observePatternTitle(patternId: PatternId?) {
        patternJob?.cancel()

        if (patternId == null) {
            _state.update { it.copy(selectedPatternTitle = null) }
            return
        }

        patternJob = viewModelScope.launch {
            getPatternByIdUseCase(patternId)
                .collect { pattern ->
                    _state.update { it.copy(selectedPatternTitle = pattern?.title) }
                }
        }
    }

    private fun save() {
        val pairId = activePairId ?: return
        val emotion = _state.value.emotion ?: return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            val updated = if (currentEmotions.any { it.id == emotion.id }) {
                currentEmotions.map { if (it.id == emotion.id) emotion else it }
            } else {
                (currentEmotions + emotion).sortedBy { it.order }
            }

            val result = updatePairEmotionsUseCase(PairId(pairId), updated)
            val error = result.component2()

            if (error != null) {
                _state.update { it.copy(isSaving = false, error = error) }
                _effects.emit(HugsEmotionEditorEffect.ShowError(error))
            } else {
                _state.update { it.copy(isSaving = false) }
                _effects.emit(HugsEmotionEditorEffect.NavigateBack)
            }
        }
    }

    private data class PairData(
        val pairId: String?,
        val emotions: List<PairEmotion>,
    )
}
