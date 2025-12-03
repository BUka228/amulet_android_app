package com.example.amulet.feature.hugs.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.domain.hugs.GetHugByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HugDetailsViewModel @Inject constructor(
    private val getHugByIdUseCase: GetHugByIdUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(HugDetailsState())
    val state: StateFlow<HugDetailsState> = _state.asStateFlow()
    
    private val _effects = MutableSharedFlow<HugDetailsEffect>()
    val effects = _effects.asSharedFlow()
    
    private var currentHugId: String? = null

    fun setHugId(hugId: String) {
        if (currentHugId == hugId) return
        currentHugId = hugId
        loadHug(hugId)
    }

    fun handleIntent(intent: HugDetailsIntent) {
        when (intent) {
            HugDetailsIntent.Refresh -> currentHugId?.let { loadHug(it) }
            HugDetailsIntent.Reply -> handleReply()
            HugDetailsIntent.Delete -> handleDelete()
        }
    }

    private fun loadHug(hugId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val hugIdValue = com.example.amulet.shared.domain.hugs.model.HugId(hugId)
            val result = getHugByIdUseCase(hugIdValue)
            
            val hug = result.component1()
            val error = result.component2()
            
            if (hug != null) {
                _state.update { it.copy(isLoading = false, hug = hug) }
            } else if (error != null) {
                _state.update { it.copy(isLoading = false, error = error) }
                emitEffect(HugDetailsEffect.ShowError(error))
            }
        }
    }

    private fun handleReply() {
        emitEffect(HugDetailsEffect.NavigateToReply)
    }

    private fun handleDelete() {
        // TODO: Implement delete logic
        emitEffect(HugDetailsEffect.NavigateBack)
    }
    
    private fun emitEffect(effect: HugDetailsEffect) {
        viewModelScope.launch {
            _effects.emit(effect)
        }
    }
}
