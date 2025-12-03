package com.example.amulet.feature.hugs.presentation.secretcodes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.domain.hugs.GetSecretCodesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HugsSecretCodesViewModel @Inject constructor(
    private val getSecretCodesUseCase: GetSecretCodesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HugsSecretCodesState())
    val state: StateFlow<HugsSecretCodesState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<HugsSecretCodesEffect>()
    val effects = _effects.asSharedFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            getSecretCodesUseCase()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                )
                .collect { codes ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            codes = codes,
                        )
                    }
                }
        }
    }

    fun onIntent(intent: HugsSecretCodesIntent) {
        when (intent) {
            HugsSecretCodesIntent.Refresh -> refresh()
            is HugsSecretCodesIntent.OpenCode -> openCode(intent.patternId)
        }
    }

    private fun refresh() {
        // Поток secret codes уже горячий (Flow из репозитория), отдельного sync пока нет
        // Оставляем заглушку на случай будущего расширения.
    }

    private fun openCode(patternId: String) {
        viewModelScope.launch {
            _effects.emit(HugsSecretCodesEffect.OpenPatternDetails(patternId))
        }
    }
}
