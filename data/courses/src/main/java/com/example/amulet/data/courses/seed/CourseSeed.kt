package com.example.amulet.data.courses.seed

import com.example.amulet.shared.domain.courses.model.CourseItemType

/**
 * Модель пресета для сидирования курсов.
 */
data class CourseSeed(
    val id: String,
    val title: String,
    val description: String?,
    val tags: List<String> = emptyList(),
    val totalDurationSec: Int? = null,
    val difficulty: String? = null,
    val coverUrl: String? = null,
    val category: String? = null,
    val items: List<CourseItemSeed> = emptyList(),
    val createdAt: Long? = System.currentTimeMillis(),
    val updatedAt: Long? = System.currentTimeMillis()
)

/**
 * Модель элемента курса для сидирования.
 */
data class CourseItemSeed(
    val id: String,
    val courseId: String,
    val order: Int,
    val type: CourseItemType,
    val practiceId: String?,
    val title: String?,
    val description: String?,
    val mandatory: Boolean = true,
    val minDurationSec: Int? = null,
    val contentUrl: String? = null
)
