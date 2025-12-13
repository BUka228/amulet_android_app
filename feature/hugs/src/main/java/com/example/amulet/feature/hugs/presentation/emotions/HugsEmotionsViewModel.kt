package com.example.amulet.feature.hugs.presentation.emotions

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
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
class HugsEmotionsViewModel @Inject constructor(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val observePairsUseCase: ObservePairsUseCase,
    private val observePairEmotionsUseCase: ObservePairEmotionsUseCase,
    private val updatePairEmotionsUseCase: UpdatePairEmotionsUseCase,
    private val getPatternByIdUseCase: GetPatternByIdUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HugsEmotionsState())
    val state: StateFlow<HugsEmotionsState> = _state.asStateFlow()

    private var activePairId: String? = null
    private var currentEmotions: List<PairEmotion> = emptyList()
    private var patternsJob: Job? = null

    init {
        observeData()
    }

    fun onIntent(intent: HugsEmotionsIntent) {
        when (intent) {
            is HugsEmotionsIntent.ToggleSelection -> toggleSelection(intent.emotionId)
            HugsEmotionsIntent.ClearSelection -> clearSelection()
            HugsEmotionsIntent.DeleteSelected -> deleteSelected()
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val userFlow = observeCurrentUserUseCase()
            val pairsFlow = observePairsUseCase()

            userFlow
                .combine(pairsFlow) { user, pairs ->
                    val activePair = pairs.firstOrNull { it.status == PairStatus.ACTIVE } ?: pairs.firstOrNull()
                    BaseData(userNameUser = user, activePair = activePair)
                }
                .flatMapLatest { base ->
                    val pair = base.activePair
                    if (pair == null) {
                        flowOf(FullData(base = base, emotions = emptyList()))
                    } else {
                        observePairEmotionsUseCase(pair.id)
                            .map { emotions -> FullData(base = base, emotions = emotions) }
                    }
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = FullData(
                        base = BaseData(userNameUser = null, activePair = null),
                        emotions = emptyList()
                    )
                )
                .collect { data ->
                    activePairId = data.base.activePair?.id?.value
                    currentEmotions = data.emotions

                    observePatternTitles(data.emotions)

                    _state.update { current ->
                        current.copy(
                            isLoading = false,
                            currentUser = data.base.userNameUser,
                            activePair = data.base.activePair,
                            emotions = data.emotions.sortedBy { it.order },
                            selectedEmotionIds = current.selectedEmotionIds.intersect(data.emotions.map { it.id }.toSet()),
                        )
                    }
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observePatternTitles(emotions: List<PairEmotion>) {
        patternsJob?.cancel()

        val ids = emotions.mapNotNull { it.patternId?.value }.distinct()
        if (ids.isEmpty()) {
            _state.update { it.copy(patternTitles = emptyMap()) }
            return
        }

        patternsJob = viewModelScope.launch {
            combine(ids.map { id ->
                getPatternByIdUseCase(PatternId(id)).map { pattern ->
                    id to pattern?.title
                }
            }) { pairs ->
                pairs
                    .mapNotNull { (id, title) -> title?.let { id to it } }
                    .toMap()
            }.collect { titles ->
                _state.update { it.copy(patternTitles = titles) }
            }
        }
    }

    private fun toggleSelection(emotionId: String) {
        _state.update { current ->
            val selected = current.selectedEmotionIds
            val next = if (selected.contains(emotionId)) {
                selected - emotionId
            } else {
                selected + emotionId
            }
            current.copy(selectedEmotionIds = next)
        }
    }

    private fun clearSelection() {
        _state.update { it.copy(selectedEmotionIds = emptySet()) }
    }

    private fun deleteSelected() {
        val pairId = activePairId ?: return
        val selectedIds = _state.value.selectedEmotionIds
        if (selectedIds.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true, error = null) }

            val updated = currentEmotions
                .filterNot { selectedIds.contains(it.id) }
                .sortedBy { it.order }

            val result = updatePairEmotionsUseCase(PairId(pairId), updated)
            val error = result.component2()

            if (error != null) {
                _state.update { it.copy(isDeleting = false, error = error) }
            } else {
                _state.update { it.copy(isDeleting = false, selectedEmotionIds = emptySet()) }
            }
        }
    }

    private data class BaseData(
        val userNameUser: com.example.amulet.shared.domain.user.model.User?,
        val activePair: com.example.amulet.shared.domain.hugs.model.Pair?,
    )

    private data class FullData(
        val base: BaseData,
        val emotions: List<PairEmotion>,
    )
}
