package com.example.amulet.feature.practices.presentation.schedule

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amulet.shared.domain.practices.model.PracticeId
import com.example.amulet.shared.domain.practices.model.PracticeSchedule
import com.example.amulet.shared.domain.practices.usecase.GetPracticeByIdUseCase
import com.example.amulet.shared.domain.practices.usecase.UpsertPracticeScheduleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PracticeScheduleViewModel @Inject constructor(
    private val getPracticeByIdUseCase: GetPracticeByIdUseCase,
    private val upsertPracticeScheduleUseCase: UpsertPracticeScheduleUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val practiceId: String = checkNotNull(savedStateHandle["practiceId"])

    private val _state = MutableStateFlow(
        PracticeScheduleState(
            practiceId = practiceId,
            practiceTitle = ""
        )
    )
    val state: StateFlow<PracticeScheduleState> = _state.asStateFlow()

    private val _effects = Channel<PracticeScheduleEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        observePracticeTitle()
    }

    private fun observePracticeTitle() {
        viewModelScope.launch {
            getPracticeByIdUseCase(practiceId).collect { practice ->
                if (practice != null) {
                    _state.update { it.copy(practiceTitle = practice.title) }
                }
            }
        }
    }

    fun onIntent(intent: PracticeScheduleIntent) {
        when (intent) {
            is PracticeScheduleIntent.ToggleDay -> toggleDay(intent.day)
            is PracticeScheduleIntent.ChangeTime -> changeTime(intent.time)
            is PracticeScheduleIntent.SetReminderEnabled -> setReminder(intent.enabled)
            PracticeScheduleIntent.Save -> save()
            PracticeScheduleIntent.NavigateBack -> sendBack()
        }
    }

    private fun toggleDay(day: Int) {
        _state.update { s ->
            val current = s.selectedDays
            val next = if (day in current) current - day else current + day
            s.copy(selectedDays = next)
        }
    }

    private fun changeTime(time: String) {
        _state.update { it.copy(timeOfDay = time) }
    }

    private fun setReminder(enabled: Boolean) {
        _state.update { it.copy(reminderEnabled = enabled) }
    }

    private fun save() {
        val snapshot = _state.value
        if (snapshot.selectedDays.isEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            val schedule = PracticeSchedule(
                id = java.util.UUID.randomUUID().toString(),
                userId = "", // userId берётся из репозитория
                practiceId = snapshot.practiceId,
                daysOfWeek = snapshot.selectedDays.toList().sorted(),
                timeOfDay = snapshot.timeOfDay,
                reminderEnabled = snapshot.reminderEnabled,
                createdAt = System.currentTimeMillis()
            )
            val result = upsertPracticeScheduleUseCase(schedule)
            val error = result.component2()
            if (error != null) {
                _state.update { it.copy(isSaving = false, error = error) }
            } else {
                _state.update { it.copy(isSaving = false, isCompleted = true) }
                sendBack()
            }
        }
    }

    private fun sendBack() {
        viewModelScope.launch {
            _effects.send(PracticeScheduleEffect.Back)
        }
    }
}
