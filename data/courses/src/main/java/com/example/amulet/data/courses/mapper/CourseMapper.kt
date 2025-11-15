package com.example.amulet.data.courses.mapper

import com.example.amulet.core.database.entity.CourseEntity
import com.example.amulet.core.database.entity.CourseItemEntity
import com.example.amulet.core.database.entity.CourseProgressEntity
import com.example.amulet.shared.domain.courses.model.Course
import com.example.amulet.shared.domain.courses.model.CourseItem
import com.example.amulet.shared.domain.courses.model.CourseItemType
import com.example.amulet.shared.domain.courses.model.CourseProgress
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

fun CourseEntity.toDomain(): Course = Course(
    id = id,
    title = title,
    description = description,
    tags = emptyList(),
    totalDurationSec = totalDurationSec,
    difficulty = difficulty,
    coverUrl = coverUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun CourseEntity.toDomain(json: Json): Course = Course(
    id = id,
    title = title,
    description = description,
    tags = tagsJson.safeParseStringList(json),
    totalDurationSec = totalDurationSec,
    difficulty = difficulty,
    coverUrl = coverUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun CourseItemEntity.toDomain(): CourseItem = CourseItem(
    id = id,
    courseId = courseId,
    order = order,
    type = CourseItemType.valueOf(type),
    practiceId = practiceId,
    title = title,
    description = description,
    mandatory = mandatory,
    minDurationSec = minDurationSec
)

fun CourseProgressEntity.toDomain(): CourseProgress = CourseProgress(
    courseId = courseId,
    completedItemIds = emptySet(),
    currentItemId = currentItemId,
    percent = percent,
    totalTimeSec = totalTimeSec,
    updatedAt = updatedAt
)

fun CourseProgressEntity.toDomain(json: Json): CourseProgress = CourseProgress(
    courseId = courseId,
    completedItemIds = completedItemIdsJson.safeParseStringSet(json),
    currentItemId = currentItemId,
    percent = percent,
    totalTimeSec = totalTimeSec,
    updatedAt = updatedAt
)

fun List<String>.toJsonArrayString(json: Json): String =
    json.encodeToString(JsonArray(this.map { JsonPrimitive(it) }))

private fun String?.safeParseStringList(json: Json): List<String> =
    runCatching {
        if (this.isNullOrBlank()) emptyList() else json.parseToJsonElement(this).jsonArray.map { it.jsonPrimitive.content }
    }.getOrElse { emptyList() }

private fun String?.safeParseStringSet(json: Json): Set<String> =
    safeParseStringList(json).toSet()
