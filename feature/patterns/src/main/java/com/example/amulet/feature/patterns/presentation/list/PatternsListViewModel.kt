package com.example.amulet.feature.patterns.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.domain.patterns.builder.PresetPatterns
import com.example.amulet.shared.domain.patterns.model.PatternDraft
import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.patterns.usecase.*
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatternsListViewModel @Inject constructor(
    private val getPatternsStreamUseCase: GetPatternsStreamUseCase,
    private val observeMyPatternsUseCase: ObserveMyPatternsUseCase,
    private val deletePatternUseCase: DeletePatternUseCase,
    private val createPatternUseCase: CreatePatternUseCase,
    private val syncPatternsUseCase: SyncPatternsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatternsListState())
    val uiState: StateFlow<PatternsListState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<PatternsListSideEffect>()
    val sideEffect: SharedFlow<PatternsListSideEffect> = _sideEffect.asSharedFlow()

    init {
        observePatterns()
    }

    fun handleEvent(event: PatternsListEvent) {
        when (event) {
            is PatternsListEvent.Refresh -> refresh()
            is PatternsListEvent.SelectTab -> selectTab(event.tab)
            is PatternsListEvent.UpdateSearchQuery -> updateSearchQuery(event.query)
            is PatternsListEvent.PatternClicked -> navigateToEditor(event.patternId)
            is PatternsListEvent.CreatePatternClicked -> navigateToEditor(null)
            is PatternsListEvent.DeletePattern -> deletePattern(event.patternId)
            is PatternsListEvent.PreviewPattern -> navigateToPreview(event.patternId)
            is PatternsListEvent.ToggleSearch -> toggleSearch()
            is PatternsListEvent.DismissError -> dismissError()
        }
    }

    private fun observePatterns() {
        combine(
            observeMyPatternsUseCase(),
            getPatternsStreamUseCase(com.example.amulet.shared.domain.patterns.model.PatternFilter(publicOnly = true))
        ) { myPatterns, publicPatterns ->
            _uiState.update {
                it.copy(
                    myPatterns = myPatterns,
                    publicPatterns = publicPatterns,
                    patterns = when (it.selectedTab) {
                        PatternTab.MY_PATTERNS -> myPatterns
                        PatternTab.PUBLIC -> publicPatterns
                        PatternTab.PRESETS -> emptyList() // Пресеты генерируются динамически
                    },
                    isLoading = false,
                    isEmpty = myPatterns.isEmpty() && publicPatterns.isEmpty()
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        
        viewModelScope.launch {
            syncPatternsUseCase()
                .onSuccess {
                    _uiState.update { it.copy(isRefreshing = false) }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isRefreshing = false,
                            error = error
                        )
                    }
                }
        }
    }

    private fun selectTab(tab: PatternTab) {
        _uiState.update {
            it.copy(
                selectedTab = tab,
                patterns = when (tab) {
                    PatternTab.MY_PATTERNS -> it.myPatterns
                    PatternTab.PUBLIC -> it.publicPatterns
                    PatternTab.PRESETS -> emptyList()
                }
            )
        }
    }

    private fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    private fun navigateToEditor(patternId: String?) {
        viewModelScope.launch {
            _sideEffect.emit(PatternsListSideEffect.NavigateToPatternEditor(patternId))
        }
    }

    private fun navigateToPreview(patternId: String) {
        viewModelScope.launch {
            _sideEffect.emit(PatternsListSideEffect.NavigateToPatternPreview(patternId))
        }
    }

    private fun deletePattern(patternId: String) {
        viewModelScope.launch {
            deletePatternUseCase(PatternId(patternId))
                .onSuccess {
                    _sideEffect.emit(PatternsListSideEffect.ShowSnackbar("Паттерн удалён"))
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error) }
                }
        }
    }


    private fun toggleSearch() {
        _uiState.update { 
            it.copy(
                isSearchActive = !it.isSearchActive,
                searchQuery = if (!it.isSearchActive) it.searchQuery else ""
            )
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
