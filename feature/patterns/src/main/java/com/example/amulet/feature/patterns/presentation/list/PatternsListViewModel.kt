package com.example.amulet.feature.patterns.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.patterns.model.PatternKind
import com.example.amulet.shared.domain.patterns.usecase.DeletePatternUseCase
import com.example.amulet.shared.domain.patterns.usecase.GetPatternsStreamUseCase
import com.example.amulet.shared.domain.patterns.usecase.GetPresetsUseCase
import com.example.amulet.shared.domain.patterns.usecase.ObserveMyPatternsUseCase
import com.example.amulet.shared.domain.patterns.usecase.SyncPatternsUseCase
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatternsListViewModel @Inject constructor(
    private val getPatternsStreamUseCase: GetPatternsStreamUseCase,
    private val observeMyPatternsUseCase: ObserveMyPatternsUseCase,
    private val deletePatternUseCase: DeletePatternUseCase,
    private val syncPatternsUseCase: SyncPatternsUseCase,
    private val getPresetsUseCase: GetPresetsUseCase
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
            is PatternsListEvent.ToggleFilterSheet -> toggleFilterSheet()
            is PatternsListEvent.HideFilterSheet -> hideFilterSheet()
            is PatternsListEvent.ToggleKindFilter -> toggleKindFilter(event.kind)
            is PatternsListEvent.ToggleTagFilter -> toggleTagFilter(event.tag)
            is PatternsListEvent.AddTagFilter -> addTagFilter(event.tag)
            is PatternsListEvent.RemoveTagFilter -> removeTagFilter(event.tag)
            is PatternsListEvent.ClearTagFilters -> clearTagFilters()
            is PatternsListEvent.ClearFilters -> clearFilters()
            is PatternsListEvent.DismissError -> dismissError()
        }
    }

    private fun observePatterns() {
        combine(
            observeMyPatternsUseCase(),
            getPatternsStreamUseCase(com.example.amulet.shared.domain.patterns.model.PatternFilter(publicOnly = true)),
            getPresetsUseCase()
        ) { myPatterns, publicPatterns, presets ->
            val allPatterns = myPatterns + publicPatterns + presets
            val availableTags = allPatterns.flatMap { it.tags }.toSet()
            
            _uiState.update {
                it.copy(
                    myPatterns = myPatterns,
                    publicPatterns = publicPatterns,
                    presets = presets,
                    availableTags = availableTags,
                    patterns = when (it.selectedTab) {
                        PatternTab.MY_PATTERNS -> myPatterns
                        PatternTab.PUBLIC -> publicPatterns
                        PatternTab.PRESETS -> presets
                    },
                    isLoading = false,
                    isEmpty = myPatterns.isEmpty() && publicPatterns.isEmpty() && presets.isEmpty()
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
                    PatternTab.PRESETS -> it.presets
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

    private fun toggleFilterSheet() {
        _uiState.update { 
            it.copy(isFilterSheetVisible = !it.isFilterSheetVisible)
        }
    }

    private fun hideFilterSheet() {
        _uiState.update { 
            it.copy(isFilterSheetVisible = false)
        }
    }

    private fun toggleKindFilter(kind: PatternKind) {
        _uiState.update { 
            val currentKinds = it.selectedKinds
            val newKinds = if (kind in currentKinds) {
                currentKinds - kind
            } else {
                currentKinds + kind
            }
            it.copy(selectedKinds = newKinds)
        }
    }

    private fun toggleTagFilter(tag: String) {
        _uiState.update { 
            val currentTags = it.selectedTags
            val newTags = if (tag in currentTags) {
                currentTags - tag
            } else {
                currentTags + tag
            }
            it.copy(selectedTags = newTags)
        }
    }

    private fun addTagFilter(tag: String) {
        _uiState.update { 
            val newTags = it.selectedTags + tag
            it.copy(
                selectedTags = newTags,
                showSelectedTagsRow = true
            )
        }
    }

    private fun removeTagFilter(tag: String) {
        _uiState.update { 
            val newTags = it.selectedTags - tag
            it.copy(
                selectedTags = newTags,
                showSelectedTagsRow = newTags.isNotEmpty()
            )
        }
    }

    private fun clearTagFilters() {
        _uiState.update { 
            it.copy(
                selectedTags = emptySet(),
                showSelectedTagsRow = false
            )
        }
    }

    private fun clearFilters() {
        _uiState.update { 
            it.copy(
                selectedKinds = emptySet(),
                selectedTags = emptySet(),
                showSelectedTagsRow = false
            )
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
