package com.example.amulet.data.practices.mapper

import com.example.amulet.core.database.entity.CollectionEntity
import com.example.amulet.core.database.entity.CollectionItemEntity
import com.example.amulet.core.database.entity.PlanEntity
import com.example.amulet.core.database.entity.PracticeTagEntity
import com.example.amulet.core.database.entity.UserBadgeEntity
import com.example.amulet.core.database.entity.UserMoodEntryEntity
import com.example.amulet.core.database.entity.UserPracticeStatsEntity
import com.example.amulet.shared.domain.practices.model.PracticeBadge
import com.example.amulet.shared.domain.practices.model.PracticeCollection
import com.example.amulet.shared.domain.practices.model.PracticeCollectionItem
import com.example.amulet.shared.domain.practices.model.PracticePlan
import com.example.amulet.shared.domain.practices.model.PracticeStatistics
import com.example.amulet.shared.domain.practices.model.PracticeTag
import com.example.amulet.shared.domain.practices.model.PracticeGoal
import com.example.amulet.shared.domain.practices.model.PracticeType
import com.example.amulet.shared.domain.practices.model.MoodEntry
import com.example.amulet.shared.domain.practices.model.MoodKind
import com.example.amulet.shared.domain.practices.model.MoodSource
import com.example.amulet.shared.domain.user.model.UserId

fun PlanEntity.toDomain(): PracticePlan = PracticePlan(
    id = id,
    userId = UserId(userId),
    title = title,
    description = description,
    status = status,
    type = type,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun UserPracticeStatsEntity.toDomain(): PracticeStatistics = PracticeStatistics(
    userId = UserId(userId),
    totalSessions = totalSessions,
    totalDurationSec = totalDurationSec,
    currentStreak = currentStreakDays,
    longestStreak = bestStreakDays,
    // Подробные разбиения по типам/целям и completionRate считаем на лету в use-case’ах
    sessionsByType = emptyMap<PracticeType, Int>(),
    sessionsByGoal = emptyMap<PracticeGoal, Int>(),
    completionRate = 0f,
    lastSessionAt = lastPracticeDate,
    updatedAt = updatedAt
)

fun UserBadgeEntity.toDomain(): PracticeBadge = PracticeBadge(
    id = id,
    userId = UserId(userId),
    code = code,
    earnedAt = earnedAt,
    metadata = metadataJson
        ?.takeIf { it.isNotBlank() }
        ?.let { json ->
            try {
                val element = kotlinx.serialization.json.Json.parseToJsonElement(json)
                val obj = element as? kotlinx.serialization.json.JsonObject
                obj?.entries?.associate { (key, value) -> key to value.toString() } ?: emptyMap()
            } catch (_: Exception) {
                emptyMap()
            }
        } ?: emptyMap()
)

fun PracticeTagEntity.toDomain(): PracticeTag = PracticeTag(
    id = id,
    name = name,
    kind = kind
)

fun CollectionEntity.toDomain(items: List<CollectionItemEntity>): PracticeCollection = PracticeCollection(
    id = id,
    code = code,
    title = title,
    description = description,
    order = order,
    items = items.map { it.toDomain() }
)

fun CollectionItemEntity.toDomain(): PracticeCollectionItem = PracticeCollectionItem(
    id = id,
    collectionId = collectionId,
    type = type,
    practiceId = practiceId,
    courseId = courseId,
    order = order
)

fun UserMoodEntryEntity.toDomain(): MoodEntry? {
    val moodKind = runCatching { MoodKind.valueOf(mood) }.getOrNull()
    val moodSource = runCatching { MoodSource.valueOf(source) }.getOrNull()
    if (moodKind == null || moodSource == null) return null

    return MoodEntry(
        id = id,
        userId = UserId(userId),
        mood = moodKind,
        source = moodSource,
        createdAt = createdAt
    )
}

fun MoodEntry.toEntity(): UserMoodEntryEntity = UserMoodEntryEntity(
    id = id,
    userId = userId.value,
    mood = mood.name,
    source = source.name,
    createdAt = createdAt
)
