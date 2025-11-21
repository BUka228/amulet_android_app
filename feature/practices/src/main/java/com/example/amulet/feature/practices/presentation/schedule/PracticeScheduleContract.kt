package com.example.amulet.feature.practices.presentation.schedule

import com.example.amulet.shared.core.AppError

data class PracticeScheduleState(
    val practiceId: String,
    val practiceTitle: String,
    val selectedDays: Set<Int> = setOf(1,2,3,4,5),
    val timeOfDay: String = "09:00",
    val reminderEnabled: Boolean = true,
    val isSaving: Boolean = false,
    val error: AppError? = null,
    val isCompleted: Boolean = false
)

sealed class PracticeScheduleIntent {
    data class ToggleDay(val day: Int) : PracticeScheduleIntent()
    data class ChangeTime(val time: String) : PracticeScheduleIntent()
    data class SetReminderEnabled(val enabled: Boolean) : PracticeScheduleIntent()
    object Save : PracticeScheduleIntent()
    object NavigateBack : PracticeScheduleIntent()
}

sealed class PracticeScheduleEffect {
    object Back : PracticeScheduleEffect()
}
