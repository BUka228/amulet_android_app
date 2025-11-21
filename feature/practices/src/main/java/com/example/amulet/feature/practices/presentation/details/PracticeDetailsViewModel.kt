package com.example.amulet.feature.practices.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.domain.practices.usecase.GetPracticeByIdUseCase
import com.example.amulet.shared.domain.practices.usecase.StartPracticeUseCase
import com.example.amulet.shared.domain.practices.usecase.SetFavoritePracticeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class PracticeDetailsViewModel @Inject constructor(
    private val getPracticeByIdUseCase: GetPracticeByIdUseCase,
    private val startPracticeUseCase: StartPracticeUseCase,
    private val getPatternByIdUseCase: com.example.amulet.shared.domain.patterns.usecase.GetPatternByIdUseCase,
    private val setFavoritePracticeUseCase: SetFavoritePracticeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PracticeDetailsState())
    val uiState: StateFlow<PracticeDetailsState> = _uiState.asStateFlow()

    private val _effect = kotlinx.coroutines.channels.Channel<PracticeDetailsEffect>()
    val effect = _effect.receiveAsFlow()

    fun setIdIfEmpty(id: String) {
        if (_uiState.value.practiceId == null) {
            _uiState.update { it.copy(practiceId = id) }
            observe()
        }
    }

    fun handleIntent(intent: PracticeDetailsIntent) {
        when (intent) {
            is PracticeDetailsIntent.StartPractice -> start()
            is PracticeDetailsIntent.ToggleFavorite -> toggleFavorite()
            is PracticeDetailsIntent.OpenPattern -> openPattern()
            is PracticeDetailsIntent.AddToPlan -> addToPlan()
            is PracticeDetailsIntent.NavigateBack -> navigateBack()
        }
    }

    private fun observe() {
        val id = _uiState.value.practiceId ?: return
        viewModelScope.launch {
            getPracticeByIdUseCase(id).collect { p ->
                _uiState.update { it.copy(isLoading = false, practice = p, isFavorite = p?.isFavorite ?: false) }
                p?.patternId?.let { patternId ->
                    launch {
                        getPatternByIdUseCase(patternId).collect { pattern ->
                            _uiState.update { it.copy(pattern = pattern) }
                        }
                    }
                }
            }
        }
    }

    private fun start() {
        val id = _uiState.value.practiceId ?: return
        viewModelScope.launch {
            startPracticeUseCase(id)
        }
    }

    private fun toggleFavorite() {
        val practiceId = _uiState.value.practiceId ?: return
        val current = _uiState.value.isFavorite
        _uiState.update { it.copy(isFavorite = !current) }
        viewModelScope.launch {
            val result = setFavoritePracticeUseCase(practiceId, !current)
            val error = result.component2()
            if (error != null) {
                _uiState.update { it.copy(isFavorite = current, error = error) }
            }
        }
    }

    private fun openPattern() {
        val patternId = _uiState.value.pattern?.id?.value ?: return
        viewModelScope.launch { _effect.send(PracticeDetailsEffect.NavigateToPattern(patternId)) }
    }

    private fun addToPlan() {
        val practiceId = _uiState.value.practiceId ?: return
        viewModelScope.launch { _effect.send(PracticeDetailsEffect.NavigateToPlan(practiceId)) }
    }

    private fun navigateBack() {
        viewModelScope.launch { _effect.send(PracticeDetailsEffect.NavigateBack) }
    }
}
