package com.example.amulet.shared.domain.courses.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Параметры для записи на курс
 */
data class EnrollmentParams(
    /** ID курса */
    val courseId: String,
    
    /** Выбранный ритм прохождения курса */
    val rhythm: CourseRhythm,
    
    /** Предпочтительное время для практик */
    val preferredTime: LocalTime,
    
    /** Выбранные дни недели для занятий */
    val selectedDays: Set<DayOfWeek>,
    
    /** Дата начала курса */
    val startDate: LocalDate
)
