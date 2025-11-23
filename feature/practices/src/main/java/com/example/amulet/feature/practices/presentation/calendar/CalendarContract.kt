package com.example.amulet.feature.practices.presentation.calendar

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.practices.model.ScheduledSession
import com.example.amulet.shared.domain.practices.model.Practice
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

enum class ScheduleViewMode {
    CALENDAR,
    LIST
}

data class CalendarState(
    val viewMode: ScheduleViewMode = ScheduleViewMode.CALENDAR,
    val selectedDate: LocalDate,
    val currentMonth: YearMonth,
    val sessions: List<ScheduledSession> = emptyList(),
    val isLoading: Boolean = false,
    val error: AppError? = null,
    val plannerAvailablePractices: List<Practice> = emptyList(),
    // Planner bottom sheet state
    val plannerPracticeId: String? = null,
    val plannerPracticeTitle: String = "",
    val plannerSelectedDays: Set<Int> = setOf(1, 2, 3, 4, 5),
    val plannerTimeOfDay: String = "09:00",
    val plannerReminderEnabled: Boolean = true,
    val isPlannerOpen: Boolean = false,
    val isPlannerSaving: Boolean = false
) {
    val sessionsForSelectedDate: List<ScheduledSession>
        get() = sessions.filter {
            // Assuming session ID format or timestamp check. 
            // Ideally we check scheduledTime against selectedDate
            // Here we will filter by timestamp in ViewModel or use a helper
            true // Placeholder, logic will be in ViewModel or here if we pass timezone
        }
}

sealed class CalendarIntent {
    data class ChangeViewMode(val mode: ScheduleViewMode) : CalendarIntent()
    data class SelectDate(val date: LocalDate) : CalendarIntent()
    data class ChangeMonth(val offset: Int) : CalendarIntent() // +1 or -1
    data class OpenSession(val sessionId: String) : CalendarIntent()
    data class StartSession(val sessionId: String) : CalendarIntent()
    data class RescheduleSession(val sessionId: String, val newTime: Long) : CalendarIntent()
    data class CancelSession(val sessionId: String) : CalendarIntent()
    // Planner
    data class OpenPlanner(val practiceId: String) : CalendarIntent()
    object OpenPlannerGlobal : CalendarIntent()
    data class PlannerSelectPractice(val practiceId: String) : CalendarIntent()
    object ClosePlanner : CalendarIntent()
    data class PlannerToggleDay(val day: Int) : CalendarIntent()
    data class PlannerChangeTime(val time: String) : CalendarIntent()
    data class PlannerSetReminderEnabled(val enabled: Boolean) : CalendarIntent()
    object PlannerSave : CalendarIntent()
    object NavigateBack : CalendarIntent()
    object Refresh : CalendarIntent()
}

sealed class CalendarEffect {
    data class NavigateToPractice(val practiceId: String) : CalendarEffect()
    data class NavigateToCourse(val courseId: String) : CalendarEffect()
    object NavigateBack : CalendarEffect()
}
