package com.example.amulet.feature.practices.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.domain.practices.model.PracticeFilter
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(PracticesSearchState())
    val uiState: StateFlow<PracticesSearchState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        observeQuery()
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        queryFlow.value = query
    }

    private fun observeQuery() {
        viewModelScope.launch {
            queryFlow
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isBlank()) {
                        _uiState.update { it.copy(results = emptyList(), isLoading = false, error = null) }
                        return@collect
                    }

                    _uiState.update { it.copy(isLoading = true, error = null) }

                    val result = searchPracticesUseCase(
                        query = query,
                        filter = PracticeFilter(),
                    )
                    val (practices, error) = result

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            results = practices ?: emptyList(),
                            error = error,
                        )
                    }
                }
        }
    }
}
