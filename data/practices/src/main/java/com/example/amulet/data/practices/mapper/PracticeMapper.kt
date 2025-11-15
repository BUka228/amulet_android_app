package com.example.amulet.data.practices.mapper

import com.example.amulet.core.database.entity.PracticeCategoryEntity
import com.example.amulet.core.database.entity.PracticeEntity
import com.example.amulet.core.database.entity.PracticeSessionEntity
import com.example.amulet.core.database.entity.UserPreferencesEntity
import com.example.amulet.shared.domain.practices.model.Practice
import com.example.amulet.shared.domain.practices.model.PracticeCategory
import com.example.amulet.shared.domain.practices.model.PracticeSession
import com.example.amulet.shared.domain.practices.model.PracticeSessionStatus
import com.example.amulet.shared.domain.practices.model.PracticeType
import com.example.amulet.shared.domain.practices.model.UserPreferences
import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.user.model.UserId
import com.example.amulet.shared.domain.devices.model.DeviceId

fun PracticeEntity.toDomain(): Practice = Practice(
    id = id,
    type = PracticeType.valueOf(type),
    title = title,
    description = description,
    durationSec = durationSec,
    patternId = patternId?.let { PatternId(it) },
    audioUrl = audioUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)

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
