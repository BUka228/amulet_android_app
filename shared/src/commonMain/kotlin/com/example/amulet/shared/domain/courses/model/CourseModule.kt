package com.example.amulet.shared.domain.courses.model

/** Модуль / день курса практик. */
data class CourseModule(
    val id: CourseModuleId,
    val courseId: CourseId,
    val order: Int,
    val title: String? = null,
    val description: String? = null,
    val recommendedDayOffset: Int? = null
)
