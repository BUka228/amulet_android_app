package com.example.amulet.shared.domain.courses.model

import com.example.amulet.shared.domain.practices.model.PracticeId

data class CourseItem(
    val id: CourseItemId,
    val courseId: CourseId,
    val order: Int,
    val type: CourseItemType,
    val practiceId: PracticeId?,
    val title: String?,
    val description: String?,
    val mandatory: Boolean = true,
    val minDurationSec: Int? = null
)
