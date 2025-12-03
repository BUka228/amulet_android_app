package com.example.amulet.feature.hugs.presentation.emotions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.domain.hugs.ObservePairEmotionsUseCase
import com.example.amulet.shared.domain.hugs.ObservePairsUseCase
import com.example.amulet.shared.domain.hugs.UpdatePairEmotionsUseCase
import com.example.amulet.shared.domain.hugs.model.PairEmotion
import com.example.amulet.shared.domain.hugs.model.PairId
import com.example.amulet.shared.domain.hugs.model.PairStatus
import com.example.amulet.shared.domain.user.usecase.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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

@HiltViewModel
class HugsEmotionsViewModel @Inject constructor(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val observePairsUseCase: ObservePairsUseCase,
    private val observePairEmotionsUseCase: ObservePairEmotionsUseCase,
    private val updatePairEmotionsUseCase: UpdatePairEmotionsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HugsEmotionsState())
    val state: StateFlow<HugsEmotionsState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<HugsEmotionsEffect>()
    val effects = _effects.asSharedFlow()

    init {
        observeData()
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
                    _state.update { current ->
                        current.copy(
                            isLoading = false,
                            currentUser = data.base.userNameUser,
                            activePair = data.base.activePair,
                            emotions = data.emotions.sortedBy { it.order },
                        )
                    }
                }
        }
    }

    private fun openPatternEditor(patternId: String?) {
        viewModelScope.launch {
            _effects.emit(HugsEmotionsEffect.OpenPatternEditor(patternId))
        }
    }

    fun onIntent(intent: HugsEmotionsIntent) {
        when (intent) {
            is HugsEmotionsIntent.EditEmotion -> startEdit(intent.emotionId)
            HugsEmotionsIntent.CancelEdit -> cancelEdit()
            is HugsEmotionsIntent.ChangeEditingName -> changeEditingName(intent.value)
            is HugsEmotionsIntent.ChangeEditingColor -> changeEditingColor(intent.colorHex)
            HugsEmotionsIntent.SaveEditing -> saveEditing()
            is HugsEmotionsIntent.OpenPatternEditor -> openPatternEditor(intent.patternId)
        }
    }

    private fun startEdit(emotionId: String) {
        val target = _state.value.emotions.firstOrNull { it.id == emotionId } ?: return
        _state.update { it.copy(editingEmotion = target) }
    }

    private fun cancelEdit() {
        _state.update { it.copy(editingEmotion = null) }
    }

    private fun changeEditingName(value: String) {
        val editing = _state.value.editingEmotion ?: return
        _state.update { it.copy(editingEmotion = editing.copy(name = value)) }
    }

    private fun changeEditingColor(colorHex: String) {
        val editing = _state.value.editingEmotion ?: return
        _state.update { it.copy(editingEmotion = editing.copy(colorHex = colorHex)) }
    }

    private fun saveEditing() {
        val editing = _state.value.editingEmotion ?: return
        val pair = _state.value.activePair ?: return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            val updatedList = _state.value.emotions.map { emotion ->
                if (emotion.id == editing.id) editing else emotion
            }

            val result = updatePairEmotionsUseCase(PairId(pair.id.value), updatedList)
            val error = result.component2()

            if (error != null) {
                _state.update { it.copy(isSaving = false, error = error) }
                _effects.emit(HugsEmotionsEffect.ShowError(error))
            } else {
                _state.update { it.copy(isSaving = false, editingEmotion = null) }
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
