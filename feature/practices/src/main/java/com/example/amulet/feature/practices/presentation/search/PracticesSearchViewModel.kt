package com.example.amulet.feature.practices.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.domain.practices.model.PracticeFilter
import com.example.amulet.shared.domain.practices.model.PracticeGoal
import com.example.amulet.shared.domain.practices.model.PracticeLevel
import com.example.amulet.shared.domain.practices.model.PracticeType
import com.example.amulet.shared.domain.practices.usecase.GetRecommendationsStreamUseCase
import com.example.amulet.shared.domain.practices.usecase.SearchPracticesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PracticesSearchViewModel @Inject constructor(
    private val searchPracticesUseCase: SearchPracticesUseCase,
    private val getRecommendationsStreamUseCase: GetRecommendationsStreamUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PracticesSearchState())
    val uiState: StateFlow<PracticesSearchState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        observeQuery()
        loadRecommendations()
    }

    fun onEvent(event: PracticesSearchEvent) {
        when (event) {
            is PracticesSearchEvent.OnQueryChange -> onQueryChange(event.query)
            is PracticesSearchEvent.OnTypeFilterChange -> onTypeFilterChange(event.type)
            is PracticesSearchEvent.OnGoalFilterChange -> onGoalFilterChange(event.goal)
            is PracticesSearchEvent.OnLevelFilterChange -> onLevelFilterChange(event.level)
            is PracticesSearchEvent.OnDurationRangeChange -> onDurationRangeChange(event.fromSec, event.toSec)
            is PracticesSearchEvent.OnHasAudioChange -> onHasAudioChange(event.hasAudio)
            is PracticesSearchEvent.OnAmuletRequiredChange -> onAmuletRequiredChange(event.amuletRequired)
            PracticesSearchEvent.OnToggleAdvancedFilters -> toggleAdvancedFilters()
            PracticesSearchEvent.OnToggleQuickFilters -> toggleQuickFilters()
            PracticesSearchEvent.OnClearFilters -> clearFilters()
        }
    }

    private fun onQueryChange(query: String) {
        _uiState.update { 
            it.copy(
                query = query,
                isQuickFiltersExpanded = query.isEmpty()
            ) 
        }
        queryFlow.value = query
    }

    private fun onTypeFilterChange(type: PracticeType?) {
        _uiState.update { it.copy(filter = it.filter.copy(type = type)) }
        performSearch()
    }

    private fun onGoalFilterChange(goal: PracticeGoal?) {
        _uiState.update { it.copy(filter = it.filter.copy(goal = goal)) }
        performSearch()
    }

    private fun onLevelFilterChange(level: PracticeLevel?) {
        _uiState.update { it.copy(filter = it.filter.copy(level = level)) }
        performSearch()
    }

    private fun onDurationRangeChange(fromSec: Int?, toSec: Int?) {
        _uiState.update {
            it.copy(
                filter = it.filter.copy(
                    durationFromSec = fromSec,
                    durationToSec = toSec
                )
            )
        }
        performSearch()
    }

    private fun onHasAudioChange(hasAudio: Boolean?) {
        _uiState.update { it.copy(filter = it.filter.copy(hasAudio = hasAudio)) }
        performSearch()
    }

    private fun onAmuletRequiredChange(amuletRequired: Boolean?) {
        _uiState.update { it.copy(filter = it.filter.copy(amuletRequired = amuletRequired)) }
        performSearch()
    }

    private fun toggleAdvancedFilters() {
        _uiState.update { it.copy(isAdvancedFiltersVisible = !it.isAdvancedFiltersVisible) }
    }

    private fun toggleQuickFilters() {
        _uiState.update { it.copy(isQuickFiltersExpanded = !it.isQuickFiltersExpanded) }
    }

    private fun clearFilters() {
        _uiState.update {
            it.copy(
                filter = PracticeFilter(),
                query = "",
                isAdvancedFiltersVisible = false
            )
        }
        queryFlow.value = ""
    }

    private fun observeQuery() {
        viewModelScope.launch {
            queryFlow
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    performSearch()
                }
        }
    }

    private fun performSearch() {
        viewModelScope.launch {
            val query = _uiState.value.query
            val filter = _uiState.value.filter

            if (query.isBlank() && filter == PracticeFilter()) {
                _uiState.update { it.copy(results = emptyList(), isLoading = false, error = null) }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = searchPracticesUseCase(
                query = query,
                filter = filter,
            )
            
            _uiState.update {
                it.copy(
                    isLoading = false,
                    results = result.component1() ?: emptyList(),
                    error = result.component2(),
                )
            }
        }
    }

    private fun loadRecommendations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRecommendations = true) }
            getRecommendationsStreamUseCase(limit = 10).collect { practices ->
                _uiState.update {
                    it.copy(
                        recommendations = practices,
                        isLoadingRecommendations = false
                    )
                }
            }
        }
    }
}
