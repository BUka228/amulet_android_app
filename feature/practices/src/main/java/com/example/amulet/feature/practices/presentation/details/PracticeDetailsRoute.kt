package com.example.amulet.feature.practices.presentation.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.practices.model.Practice
import com.example.amulet.shared.domain.practices.usecase.GetPracticeByIdUseCase
import com.example.amulet.shared.domain.practices.usecase.StartPracticeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun PracticeDetailsRoute(
    practiceId: String,
    onNavigateBack: () -> Unit,
    viewModel: PracticeDetailsViewModel = hiltViewModel()
) {
    // Provide id to VM if needed
    viewModel.setIdIfEmpty(practiceId)
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = state.practice?.title ?: "Практика", style = MaterialTheme.typography.headlineSmall)
        Text(text = state.practice?.description ?: "", style = MaterialTheme.typography.bodyMedium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.start() }) { Text("Старт") }
            Button(onClick = onNavigateBack) { Text("Назад") }
        }
        state.error?.let { Text("Ошибка: ${it}", color = MaterialTheme.colorScheme.error) }
    }
}

data class PracticeDetailsState(
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val practiceId: String? = null,
    val practice: Practice? = null
)

@HiltViewModel
class PracticeDetailsViewModel @Inject constructor(
    private val getPracticeByIdUseCase: GetPracticeByIdUseCase,
    private val startPracticeUseCase: StartPracticeUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(PracticeDetailsState())
    val uiState: StateFlow<PracticeDetailsState> = _uiState.asStateFlow()

    fun setIdIfEmpty(id: String) {
        if (_uiState.value.practiceId == null) {
            _uiState.update { it.copy(practiceId = id) }
            observe()
        }
    }

    private fun observe() {
        val id = _uiState.value.practiceId ?: return
        viewModelScope.launch {
            getPracticeByIdUseCase(id).collect { p ->
                _uiState.update { it.copy(isLoading = false, practice = p) }
            }
        }
    }

    fun start() {
        val id = _uiState.value.practiceId ?: return
        viewModelScope.launch { startPracticeUseCase(id) }
    }
}
