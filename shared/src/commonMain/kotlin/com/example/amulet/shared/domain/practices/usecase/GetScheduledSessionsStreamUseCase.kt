package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.domain.practices.model.ScheduledSession
import com.example.amulet.shared.domain.practices.model.ScheduledSessionStatus
import com.example.amulet.shared.domain.practices.model.PracticeFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

import com.example.amulet.shared.domain.practices.PracticesRepository
import com.example.amulet.shared.domain.practices.model.PracticeSchedule
import com.example.amulet.shared.domain.courses.CoursesRepository
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.LocalDateTime
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.days
@OptIn(kotlin.time.ExperimentalTime::class)
class GetScheduledSessionsStreamUseCase(
    private val repository: PracticesRepository,
    private val coursesRepository: CoursesRepository
) {
    operator fun invoke(): Flow<List<ScheduledSession>> {
        return combine(
            repository.getSchedulesStream(),
            repository.getPracticesStream(PracticeFilter()),
            coursesRepository.getCoursesStream()
        ) { schedules, practices, courses ->
            val now = Clock.System.now()
            val timeZone = TimeZone.currentSystemDefault()
            val today = now.toLocalDateTime(timeZone).date
            val practicesMap = practices.associateBy { it.id }
            val coursesMap = courses.associateBy { it.id }

            schedules.mapNotNull { schedule ->
                // Проверяем, запланирована ли сессия на сегодня (1=Понедельник, 7=Воскресенье)
                val todayDayOfWeek = today.dayOfWeek.ordinal + 1 // ISO-8601: Понедельник = 1
                if (todayDayOfWeek !in schedule.daysOfWeek) return@mapNotNull null

                val (hour, minute) = schedule.timeOfDay.split(":").map { it.toInt() }
                val scheduledTime = LocalDateTime(today, kotlinx.datetime.LocalTime(hour, minute, 0, 0))
                val scheduledInstant = scheduledTime.toInstant(timeZone)

                // Если запланированное время сегодня и прошло не более 1 часа, показываем его
                // Или если оно в будущем сегодня
                val oneHourAgo = now - 1.hours
                val oneDayLater = now + 1.days
                if (scheduledInstant > oneHourAgo && scheduledInstant < oneDayLater) {
                     ScheduledSession(
                        id = "${schedule.id}_${today}", // Generate a session ID
                        practiceId = schedule.practiceId,
                        practiceTitle = practicesMap[schedule.practiceId]?.title ?: "Практика",
                        courseId = schedule.courseId,
                        scheduledTime = scheduledInstant.toEpochMilliseconds(),
                        status = ScheduledSessionStatus.PLANNED,
                        courseTitle = schedule.courseId?.let { id -> coursesMap[id]?.title }
                    )
                } else {
                    null
                }
            }.sortedBy { it.scheduledTime }
        }
    }
}
