package com.example.amulet.shared.domain.courses.usecase

import com.example.amulet.shared.domain.courses.model.CourseItem
import com.example.amulet.shared.domain.courses.model.EnrollmentParams
import com.example.amulet.shared.domain.practices.model.PracticeSchedule
import kotlinx.datetime.DayOfWeek
import java.util.UUID

/**
 * Use case для генерации расписания практик по курсу
 */
class CreateScheduleForCourseUseCase {
    /**
     * Генерирует список расписаний (PracticeSchedule) для курса на основе параметров записи
     * 
     * @param params Параметры записи на курс
     * @param courseItems Список практик курса (обязательные)
     * @return Список расписаний для каждой практики курса
     */
    operator fun invoke(
        params: EnrollmentParams,
        courseItems: List<CourseItem>
    ): List<PracticeSchedule> {
        val mandatoryItems = courseItems.filter { it.mandatory }.sortedBy { it.order }
        
        if (mandatoryItems.isEmpty()) return emptyList()
        
        val schedules = mutableListOf<PracticeSchedule>()
        val now = System.currentTimeMillis()
        
        // Создаём одно расписание для каждой практики курса
        // Они будут генерировать ScheduledSession автоматически
        mandatoryItems.forEach { item ->
            item.practiceId?.let { practiceId ->
                val schedule = PracticeSchedule(
                    id = UUID.randomUUID().toString(),
                    userId = "", // Будет заполнено в репозитории
                    practiceId = practiceId,
                    courseId = params.courseId,
                    daysOfWeek = params.selectedDays.map { it.isoDayNumber }.sorted(),
                    timeOfDay = "${params.preferredTime.hour.toString().padStart(2, '0')}:${params.preferredTime.minute.toString().padStart(2, '0')}",
                    reminderEnabled = true,
                    createdAt = now,
                    updatedAt = now,
                    planId = null
                )
                schedules.add(schedule)
            }
        }
        
        return schedules
    }
    
    // Преобразование DayOfWeek в ISO день (1=Понедельник, 7=Воскресенье)
    private val DayOfWeek.isoDayNumber: Int
        get() = when (this) {
            DayOfWeek.MONDAY -> 1
            DayOfWeek.TUESDAY -> 2
            DayOfWeek.WEDNESDAY -> 3
            DayOfWeek.THURSDAY -> 4
            DayOfWeek.FRIDAY -> 5
            DayOfWeek.SATURDAY -> 6
            DayOfWeek.SUNDAY -> 7
        }
}

