package com.example.amulet.feature.practices.presentation.details

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.domain.practices.PracticeSessionManager
import com.example.amulet.shared.domain.practices.model.PracticeSessionSource
import com.example.amulet.shared.domain.practices.usecase.GetPracticeByIdUseCase
import com.example.amulet.shared.domain.practices.usecase.SetFavoritePracticeUseCase
import com.example.amulet.shared.domain.courses.usecase.GetCoursesByPracticeIdUseCase
import com.example.amulet.shared.domain.practices.model.PracticeId
import com.example.amulet.shared.domain.devices.usecase.ObserveDeviceSessionStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class PracticeDetailsViewModel @Inject constructor(
    private val getPracticeByIdUseCase: GetPracticeByIdUseCase,
    private val practiceSessionManager: PracticeSessionManager,
    private val getPatternByIdUseCase: com.example.amulet.shared.domain.patterns.usecase.GetPatternByIdUseCase,
    private val setFavoritePracticeUseCase: SetFavoritePracticeUseCase,
    private val getCoursesByPracticeIdUseCase: GetCoursesByPracticeIdUseCase,
    private val observeDeviceSessionStatusUseCase: ObserveDeviceSessionStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PracticeDetailsState())
    val uiState: StateFlow<PracticeDetailsState> = _uiState.asStateFlow()

    private val _effect = kotlinx.coroutines.channels.Channel<PracticeDetailsEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        Log.d(TAG, "init: start observing connection state")
        observeConnectionState()
    }

    fun setIdIfEmpty(id: String) {
        if (_uiState.value.practiceId == null) {
            Log.d(TAG, "setIdIfEmpty: set practiceId=$id and start observe()")
            _uiState.update { it.copy(practiceId = id) }
            observe()
        } else {
            Log.d(TAG, "setIdIfEmpty: practiceId already set=${_uiState.value.practiceId}, skip")
        }
    }

    fun handleIntent(intent: PracticeDetailsIntent) {
        Log.d(TAG, "handleIntent: $intent")
        when (intent) {
            is PracticeDetailsIntent.StartPractice -> start()
            is PracticeDetailsIntent.ToggleFavorite -> toggleFavorite()
            is PracticeDetailsIntent.OpenPattern -> openPattern()
            is PracticeDetailsIntent.AddToPlan -> addToPlan()
            is PracticeDetailsIntent.OpenCourse -> openCourse(intent.courseId)
            is PracticeDetailsIntent.OpenPairing -> openPairing()
            is PracticeDetailsIntent.NavigateBack -> navigateBack()
            is PracticeDetailsIntent.OpenEditor -> openEditor()
        }
    }

    private fun observe() {
        val id = _uiState.value.practiceId ?: return
        Log.d(TAG, "observe: start observing practiceId=$id")
        viewModelScope.launch {
            getPracticeByIdUseCase(id).collect { p ->
                Log.d(TAG, "observe: practice loaded id=${p?.id} isFavorite=${p?.isFavorite}")
                _uiState.update { it.copy(isLoading = false, practice = p, isFavorite = p?.isFavorite ?: false) }
                p?.patternId?.let { patternId ->
                    launch {
                        Log.d(TAG, "observe: start observing patternId=${patternId.value}")
                        getPatternByIdUseCase(patternId).collect { pattern ->
                            Log.d(TAG, "observe: pattern loaded id=${pattern?.id?.value}")
                            _uiState.update { it.copy(pattern = pattern) }
                        }
                    }
                }
                p?.id?.let { practiceId ->
                    launch {
                        Log.d(TAG, "observe: start observing courses for practiceId=${practiceId}")
                        getCoursesByPracticeIdUseCase(practiceId).collect { courses ->
                            Log.d(TAG, "observe: courses loaded count=${courses.size}")
                            _uiState.update { it.copy(courses = courses) }
                        }
                    }
                }
            }
        }
    }

    private fun observeConnectionState() {
        observeDeviceSessionStatusUseCase()
            .onEach { sessionStatus ->
                Log.d(TAG, "observeConnectionState: status=${'$'}{sessionStatus.connection}")
                _uiState.update { it.copy(connectionStatus = sessionStatus.connection) }
            }
            .launchIn(viewModelScope)
    }

    private fun start() {
        val id = _uiState.value.practiceId ?: return
        Log.d(TAG, "start: navigate to session for practiceId=$id")
        viewModelScope.launch {
            // Больше не запускаем сессию отсюда, только навигируем на экран сессии.
            _effect.send(PracticeDetailsEffect.NavigateToSession(id))
        }
    }

    private fun toggleFavorite() {
        val practiceId = _uiState.value.practiceId ?: return
        val current = _uiState.value.isFavorite
        Log.d(TAG, "toggleFavorite: practiceId=$practiceId current=$current -> new=${!current}")
        _uiState.update { it.copy(isFavorite = !current) }
        viewModelScope.launch {
            val result = setFavoritePracticeUseCase(practiceId, !current)
            val error = result.component2()
            if (error != null) {
                Log.d(TAG, "toggleFavorite: error=$error, rollback isFavorite to $current")
                _uiState.update { it.copy(isFavorite = current, error = error) }
            } else {
                Log.d(TAG, "toggleFavorite: success")
            }
        }
    }

    private fun openPattern() {
        val patternId = _uiState.value.pattern?.id?.value ?: return
        Log.d(TAG, "openPattern: patternId=$patternId")
        viewModelScope.launch { _effect.send(PracticeDetailsEffect.NavigateToPattern(patternId)) }
    }

    private fun addToPlan() {
        val practiceId = _uiState.value.practiceId ?: return
        Log.d(TAG, "addToPlan: practiceId=$practiceId")
        viewModelScope.launch { _effect.send(PracticeDetailsEffect.NavigateToPlan(practiceId)) }
    }

    private fun openCourse(courseId: String) {
        Log.d(TAG, "openCourse: courseId=$courseId")
        viewModelScope.launch { _effect.send(PracticeDetailsEffect.NavigateToCourse(courseId)) }
    }

    private fun openPairing() {
        Log.d(TAG, "openPairing")
        viewModelScope.launch { _effect.send(PracticeDetailsEffect.NavigateToPairing) }
    }

    private fun navigateBack() {
        Log.d(TAG, "navigateBack")
        viewModelScope.launch { _effect.send(PracticeDetailsEffect.NavigateBack) }
    }

    private fun openEditor() {
        val practiceId = _uiState.value.practiceId ?: return
        Log.d(TAG, "openEditor: practiceId=$practiceId")
        viewModelScope.launch { _effect.send(PracticeDetailsEffect.NavigateToEditor(practiceId)) }
    }

    private fun openEditor() {
        val practiceId = _uiState.value.practiceId ?: return
        Log.d(TAG, "openEditor: practiceId=$practiceId")
        viewModelScope.launch { _effect.send(PracticeDetailsEffect.NavigateToEditor(practiceId)) }
    }

    private companion object {
        const val TAG = "PracticeDetailsVM"
    }
}
