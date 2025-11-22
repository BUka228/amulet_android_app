package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.domain.practices.PracticesRepository
import com.example.amulet.shared.domain.practices.model.PracticeFilter
import com.example.amulet.shared.domain.practices.model.PracticeSession
import com.example.amulet.shared.domain.practices.model.PracticeSessionSource
import com.example.amulet.shared.domain.practices.model.ScheduledSession
import com.example.amulet.shared.domain.practices.model.ScheduledSessionStatus
import com.example.amulet.shared.domain.practices.model.parsePracticeSessionSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.plus
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.isoDayNumber
import kotlin.time.ExperimentalTime

class GetScheduledSessionsForDateRangeUseCase(
    private val repository: PracticesRepository
) {
    @OptIn(ExperimentalTime::class)
    operator fun invoke(startDate: LocalDate, endDate: LocalDate): Flow<List<ScheduledSession>> {
        return combine(
            repository.getSchedulesStream(),
            repository.getPracticesStream(PracticeFilter()),
            repository.getSessionsHistoryStream(null)
        ) { schedules, practices, sessionsHistory ->
            val practicesMap = practices.associateBy { it.id }
            val skippedIds: Set<String> = sessionsHistory.mapNotNull { session: PracticeSession ->
                when (val source = parsePracticeSessionSource(session.source)) {
                    is PracticeSessionSource.ScheduleSkip -> source.scheduledId
                    else -> null
                }
            }.toSet()
            val timeZone = TimeZone.currentSystemDefault()
            val sessions = mutableListOf<ScheduledSession>()

            // Проходим по каждому дню в диапазоне
            var currentDate = startDate
            while (currentDate <= endDate) {
                // Номер дня ISO: Понедельник = 1, Воскресенье = 7
                val dayOfWeek = currentDate.dayOfWeek.isoDayNumber

                // Находим расписания, активные для этого дня недели
                val dailySchedules = schedules.filter { dayOfWeek in it.daysOfWeek }

                for (schedule in dailySchedules) {
                    val (hour, minute) = schedule.timeOfDay.split(":").map { it.toInt() }
                    val scheduledDateTime = LocalDateTime(currentDate, LocalTime(hour, minute))
                    val scheduledInstant = scheduledDateTime.toInstant(timeZone)

                    val scheduledId = "${schedule.id}_${currentDate}"
                    if (scheduledId in skippedIds) {
                        currentDate = currentDate.plus(DatePeriod(days = 1))
                        continue
                    }

                    sessions.add(
                        ScheduledSession(
                            id = scheduledId,
                            practiceId = schedule.practiceId,
                            practiceTitle = practicesMap[schedule.practiceId]?.title ?: "Практика",
                            courseId = schedule.courseId,
                            scheduledTime = scheduledInstant.toEpochMilliseconds(),
                            status = ScheduledSessionStatus.PLANNED,
                            durationSec = practicesMap[schedule.practiceId]?.durationSec
                        )
                    )
                }
                currentDate = currentDate.plus(DatePeriod(days = 1))
            }
            sessions.sortedBy { it.scheduledTime }
        }
    }
}
