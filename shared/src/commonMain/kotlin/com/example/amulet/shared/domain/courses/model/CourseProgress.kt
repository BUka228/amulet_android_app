package com.example.amulet.shared.domain.courses.model

data class CourseProgress(
    val courseId: CourseId,
    val completedItemIds: Set<CourseItemId> = emptySet(),
    val currentItemId: CourseItemId? = null,
    val percent: Int = 0,
    val totalTimeSec: Int = 0,
    val updatedAt: Long?
)
