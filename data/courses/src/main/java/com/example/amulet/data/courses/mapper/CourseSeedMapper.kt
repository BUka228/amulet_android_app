package com.example.amulet.data.courses.mapper

import com.example.amulet.core.database.entity.CourseEntity
import com.example.amulet.core.database.entity.CourseItemEntity
import com.example.amulet.core.database.entity.CourseModuleEntity
import com.example.amulet.data.courses.seed.CourseSeed
import com.example.amulet.data.courses.seed.CourseItemSeed
import com.example.amulet.data.courses.seed.CourseModuleSeed
import com.example.amulet.shared.domain.courses.model.CourseItemType
import com.example.amulet.shared.domain.courses.model.UnlockCondition
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.encodeToString

/**
 * Мапперы для преобразования CourseSeed и CourseItemSeed в Entity.
 */

fun CourseSeed.toEntity(): CourseEntity = CourseEntity(
    id = id,
    title = title,
    description = description,
    goal = goal?.name,
    level = level?.name,
    rhythm = rhythm.name,
    tagsJson = tags.toJsonArrayString(),
    totalDurationSec = totalDurationSec,
    modulesCount = modulesCount,
    recommendedDays = recommendedDays,
    difficulty = difficulty,
    coverUrl = coverUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun CourseItemSeed.toEntity(): CourseItemEntity = CourseItemEntity(
    id = id,
    courseId = courseId,
    order = order,
    type = type.name,
    practiceId = practiceId,
    title = title,
    description = description,
    mandatory = mandatory,
    minDurationSec = minDurationSec,
    moduleId = moduleId,
    unlockConditionJson = unlockCondition?.toJson()
)

fun CourseModuleSeed.toEntity(): CourseModuleEntity = CourseModuleEntity(
    id = id,
    courseId = courseId,
    order = order,
    title = title,
    description = description,
    recommendedDayOffset = recommendedDayOffset
)

fun List<String>.toJsonArrayString(): String =
    kotlinx.serialization.json.Json.encodeToString(JsonArray(this.map { JsonPrimitive(it) }))

/**
 * Сериализует UnlockCondition в JSON строку.
 */
fun UnlockCondition.toJson(): String = 
    Json.encodeToString(this)
