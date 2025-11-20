package com.example.amulet.data.courses.seed

import com.example.amulet.shared.domain.courses.model.Course
import com.example.amulet.shared.domain.courses.model.CourseItem
import com.example.amulet.shared.domain.courses.model.CourseItemType
import com.example.amulet.shared.domain.courses.model.CourseRhythm
import com.example.amulet.shared.domain.practices.model.PracticeGoal
import com.example.amulet.shared.domain.practices.model.PracticeLevel

/**
 * Модель пресета для сидирования курсов.
 */
data class CourseSeed(
    val id: String,
    val title: String,
    val description: String?,
    val goal: PracticeGoal? = null,
    val level: PracticeLevel? = null,
    val rhythm: CourseRhythm = CourseRhythm.DAILY,
    val tags: List<String> = emptyList(),
    val totalDurationSec: Int? = null,
    val modulesCount: Int = 0,
    val recommendedDays: Int? = null,
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

/**
 * Конвертация из domain модели Course в CourseSeed
 */
fun Course.toSeed(items: List<CourseItem> = emptyList()): CourseSeed = CourseSeed(
    id = id,
    title = title,
    description = description,
    goal = goal,
    level = level,
    rhythm = rhythm,
    tags = tags,
    totalDurationSec = totalDurationSec,
    modulesCount = modulesCount,
    recommendedDays = recommendedDays,
    difficulty = difficulty,
    coverUrl = coverUrl,
    createdAt = createdAt,
    updatedAt = updatedAt,
    items = items.map { it.toItemSeed() }
)

fun CourseItem.toItemSeed(): CourseItemSeed = CourseItemSeed(
    id = id,
    courseId = courseId,
    order = order,
    type = type,
    practiceId = practiceId,
    title = title,
    description = description,
    mandatory = mandatory,
    minDurationSec = minDurationSec
)
