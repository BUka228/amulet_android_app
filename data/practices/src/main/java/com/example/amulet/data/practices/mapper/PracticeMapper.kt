package com.example.amulet.data.practices.mapper

import com.example.amulet.core.database.entity.PracticeCategoryEntity
import com.example.amulet.core.database.entity.PracticeEntity
import com.example.amulet.core.database.entity.PracticeSessionEntity
import com.example.amulet.core.database.entity.UserPreferencesEntity
import com.example.amulet.shared.domain.practices.model.Practice
import com.example.amulet.shared.domain.practices.model.PracticeCategory
import com.example.amulet.shared.domain.practices.model.PracticeId
import com.example.amulet.shared.domain.practices.model.PracticeLevel
import com.example.amulet.shared.domain.practices.model.PracticeGoal
import com.example.amulet.shared.domain.practices.model.PracticeSession
import com.example.amulet.shared.domain.practices.model.PracticeSessionStatus
import com.example.amulet.shared.domain.practices.model.PracticeType
import com.example.amulet.shared.domain.practices.model.UserPreferences
import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.user.model.UserId
import com.example.amulet.shared.domain.devices.model.DeviceId

fun PracticeEntity.toDomain(): Practice {
    val tags = try {
        if (tagsJson.isBlank() || tagsJson == "[]") emptyList()
        else kotlinx.serialization.json.Json.decodeFromString<List<String>>(tagsJson)
    } catch (e: Exception) { emptyList() }
    
    val contraindications = try {
        if (contraindicationsJson.isBlank() || contraindicationsJson == "[]") emptyList()
        else kotlinx.serialization.json.Json.decodeFromString<List<String>>(contraindicationsJson)
    } catch (e: Exception) { emptyList() }
    
    val stepsJsonLocal = stepsJson
    val steps = try {
        if (stepsJsonLocal.isNullOrBlank() || stepsJsonLocal == "[]") emptyList()
        else kotlinx.serialization.json.Json.decodeFromString<List<String>>(stepsJsonLocal)
    } catch (e: Exception) { emptyList() }

    val safetyNotesJsonLocal = safetyNotesJson
    val safetyNotes = try {
        if (safetyNotesJsonLocal.isNullOrBlank() || safetyNotesJsonLocal == "[]") emptyList()
        else kotlinx.serialization.json.Json.decodeFromString<List<String>>(safetyNotesJsonLocal)
    } catch (e: Exception) { emptyList() }
    
    return Practice(
        id = id,
        type = PracticeType.valueOf(type),
        title = title,
        description = description,
        durationSec = durationSec,
        level = level?.let { PracticeLevel.valueOf(it) },
        goal = goal?.let { PracticeGoal.valueOf(it) },
        tags = tags,
        contraindications = contraindications,
        patternId = patternId?.let { PatternId(it) },
        audioUrl = audioUrl,
        isFavorite = false, // Set separately via favorites table
        usageCount = usageCount,
        createdAt = createdAt,
        updatedAt = updatedAt,
        steps = steps,
        safetyNotes = safetyNotes
    )
}

fun PracticeSessionEntity.toDomain(): PracticeSession = PracticeSession(
    id = id,
    userId = UserId(userId),
    practiceId = practiceId,
    deviceId = deviceId?.let { DeviceId(it) },
    status = PracticeSessionStatus.valueOf(status),
    startedAt = startedAt,
    completedAt = completedAt,
    durationSec = durationSec,
    intensity = intensity,
    brightness = brightness,
    completed = completed
)

fun PracticeCategoryEntity.toDomain(): PracticeCategory = PracticeCategory(
    id = id,
    title = title,
    order = order
)

fun UserPreferencesEntity.toDomain(
    goals: List<String>,
    interests: List<String>,
    durations: List<Int>
): UserPreferences = UserPreferences(
    defaultIntensity = defaultIntensity,
    defaultBrightness = defaultBrightness,
    goals = goals,
    interests = interests,
    preferredDurationsSec = durations
)
