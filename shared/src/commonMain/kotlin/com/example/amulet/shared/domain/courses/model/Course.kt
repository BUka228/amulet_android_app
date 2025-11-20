package com.example.amulet.shared.domain.courses.model

import com.example.amulet.shared.domain.practices.model.PracticeGoal
import com.example.amulet.shared.domain.practices.model.PracticeLevel

data class Course(
    val id: CourseId,
    val title: String,
    val description: String?,
    val goal: PracticeGoal?,
    val level: PracticeLevel?,
    val rhythm: CourseRhythm = CourseRhythm.DAILY,
    val tags: List<String> = emptyList(),
    val totalDurationSec: Int? = null,
    val modulesCount: Int = 0,
    val recommendedDays: Int? = null,
    val difficulty: String? = null,
    val coverUrl: String? = null,
    val createdAt: Long?,
    val updatedAt: Long?
)
