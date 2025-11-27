package com.example.amulet.feature.practices.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.core.foreground.PracticeForegroundLauncher
import com.example.amulet.shared.domain.devices.model.DeviceSessionStatus
import com.example.amulet.shared.domain.devices.usecase.ObserveDeviceSessionStatusUseCase
import com.example.amulet.shared.domain.practices.PracticeSessionManager
import com.example.amulet.shared.domain.practices.usecase.CompletePracticeSessionUseCase
import com.example.amulet.shared.domain.practices.usecase.GetPracticeByIdUseCase
import com.example.amulet.shared.domain.practices.usecase.GetUserPreferencesStreamUseCase
import com.example.amulet.shared.domain.practices.usecase.UpdateUserPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PracticeSessionViewModel @Inject constructor(
    private val practiceSessionManager: PracticeSessionManager,
    private val practiceForegroundLauncher: PracticeForegroundLauncher,
    private val getPracticeByIdUseCase: GetPracticeByIdUseCase,
    private val observeDeviceSessionStatusUseCase: ObserveDeviceSessionStatusUseCase,
    private val getUserPreferencesStreamUseCase: GetUserPreferencesStreamUseCase,
    private val updateUserPreferencesUseCase: UpdateUserPreferencesUseCase,
    private val completePracticeSessionUseCase: CompletePracticeSessionUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(PracticeSessionState())
    val state: StateFlow<PracticeSessionState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<PracticeSessionEffect>()
    val effects = _effects.asSharedFlow()

    init {
        observeSession()
    }

    fun setPracticeIdIfEmpty(id: String) {
        if (_state.value.practiceId == null) {
            _state.update { it.copy(practiceId = id) }
        }
    }

    fun handleIntent(intent: PracticeSessionIntent) {
        when (intent) {
            PracticeSessionIntent.Start -> start()
            is PracticeSessionIntent.Stop -> stop(intent.completed)
            is PracticeSessionIntent.ChangeBrightness -> changeBrightness(intent.level)
            is PracticeSessionIntent.ChangeAudioMode -> changeAudioMode(intent.mode)
            is PracticeSessionIntent.Rate -> rateSession(intent.rating, intent.note)
            PracticeSessionIntent.NavigateBack -> emitEffect(PracticeSessionEffect.NavigateBack)
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            val practiceId = _state.value.practiceId

            val practiceFlow = practiceId?.let { getPracticeByIdUseCase(it) }
            val deviceSessionFlow = observeDeviceSessionStatusUseCase()
            val userPrefsFlow = getUserPreferencesStreamUseCase()

            combine(
                practiceSessionManager.activeSession,
                practiceSessionManager.progress,
                practiceFlow ?: kotlinx.coroutines.flow.flowOf(null),
                deviceSessionFlow,
                userPrefsFlow,
            ) { session, progress, practice, deviceSession, prefs ->
                val deviceStatus: DeviceSessionStatus = deviceSession
                _state.update {
                    it.copy(
                        isLoading = false,
                        session = session,
                        progress = progress,
                        practice = practice,
                        title = practice?.title,
                        goal = practice?.goal?.name,
                        type = practice?.type?.name,
                        totalDurationSec = progress?.totalSec ?: practice?.durationSec,
                        brightnessLevel = prefs?.defaultBrightness,
                        vibrationLevel = prefs?.defaultIntensity,
                        audioMode = session?.audioMode,
                        connectionState = deviceStatus.connection,
                        batteryLevel = deviceStatus.liveStatus?.batteryLevel,
                        isCharging = deviceStatus.liveStatus?.isCharging ?: false,
                        isDeviceOnline = deviceStatus.liveStatus?.isOnline ?: false,
                        patternName = practice?.patternId?.value,
                    )
                }
            }.collect { }
        }
    }

    private fun start() {
        val current = _state.value.session
        if (current?.status == com.example.amulet.shared.domain.practices.model.PracticeSessionStatus.ACTIVE) {
            // Уже есть активная сессия — не создаём новую.
            return
        }

        val id = _state.value.practiceId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = practiceSessionManager.startSession(id)
            val error = result.component2()
            if (error != null) {
                _state.update { it.copy(isLoading = false, error = error) }
                emitEffect(PracticeSessionEffect.ShowError(error))
            } else {
                val session = result.component1()
                _state.update { it.copy(isLoading = false, session = session, error = null) }
                practiceForegroundLauncher.ensureServiceStarted()
            }
        }
    }

    private fun stop(completed: Boolean) {
        viewModelScope.launch {
            val result = practiceSessionManager.stopSession(completed)
            val error = result.component2()
            if (error != null) {
                emitEffect(PracticeSessionEffect.ShowError(error))
            } else {
                emitEffect(PracticeSessionEffect.NavigateBack)
            }
        }
    }

    private fun changeBrightness(level: Double) {
        _state.update { it.copy(brightnessLevel = level) }
        viewModelScope.launch {
            val current = getUserPreferencesStreamUseCase().firstOrNull()
            val updated = current?.copy(defaultBrightness = level)
            if (updated != null) {
                updateUserPreferencesUseCase(updated)
            }
        }
    }

    private fun changeAudioMode(mode: com.example.amulet.shared.domain.practices.model.PracticeAudioMode) {
        _state.update { it.copy(audioMode = mode) }
        // Актуальный audioMode для сессии будет учитываться при следующем старте.
    }

    private fun rateSession(rating: Int?, note: String?) {
        _state.update { it.copy(pendingRating = rating, pendingNote = note) }
        viewModelScope.launch {
            val result = completePracticeSessionUseCase(rating, note)
            val error = result.component2()
            if (error != null) {
                emitEffect(PracticeSessionEffect.ShowError(error))
            } else {
                emitEffect(PracticeSessionEffect.NavigateBack)
            }
        }
    }

    private fun emitEffect(effect: PracticeSessionEffect) {
        viewModelScope.launch {
            _effects.emit(effect)
        }
    }
}
