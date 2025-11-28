package com.example.amulet.data.practices.mapper

import com.example.amulet.core.database.entity.PracticeCategoryEntity
import com.example.amulet.core.database.entity.PracticeEntity
import com.example.amulet.core.database.entity.PracticeSessionEntity
import com.example.amulet.core.database.entity.UserPreferencesEntity
import com.example.amulet.data.practices.seed.PracticeScriptSeedData
import com.example.amulet.shared.domain.practices.model.Practice
import com.example.amulet.shared.domain.practices.model.PracticeCategory
import com.example.amulet.shared.domain.practices.model.PracticeId
import com.example.amulet.shared.domain.practices.model.PracticeLevel
import com.example.amulet.shared.domain.practices.model.PracticeGoal
import com.example.amulet.shared.domain.practices.model.PracticeSession
import com.example.amulet.shared.domain.practices.model.PracticeAudioMode
import com.example.amulet.shared.domain.practices.model.PracticeScript
import com.example.amulet.shared.domain.practices.model.PracticeStep
import com.example.amulet.shared.domain.practices.model.PracticeStepType
import com.example.amulet.shared.domain.practices.model.parsePracticeSessionSource
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

    val practiceType = PracticeType.valueOf(type)

    val seededScript = PracticeScriptSeedData.getScriptForPractice(id)

    val script = if (seededScript != null) {
        seededScript
    } else if (steps.isEmpty()) {
        null
    } else {
        val perStepDurationSec: Int? = when (practiceType) {
            PracticeType.BREATH, PracticeType.SOUND ->
                durationSec?.takeIf { it > 0 && steps.isNotEmpty() }?.let { total ->
                    (total / steps.size).coerceAtLeast(1)
                }
            else -> null
        }

        PracticeScript(
            steps = steps.mapIndexed { index, text ->
                val stepType = when (practiceType) {
                    PracticeType.BREATH -> PracticeStepType.BREATH_STEP
                    PracticeType.SOUND -> PracticeStepType.SOUND_SCAPE
                    PracticeType.MEDITATION -> if (title.contains("сканирование тела", ignoreCase = true)) {
                        PracticeStepType.BODY_SCAN
                    } else {
                        PracticeStepType.TEXT_HINT
                    }
                }
                PracticeStep(
                    order = index,
                    type = stepType,
                    title = null,
                    description = text,
                    durationSec = perStepDurationSec,
                    patternId = null,
                    audioUrl = null,
                    extra = emptyMap()
                )
            }
        )
    }
    
    return Practice(
        id = id,
        type = practiceType,
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
        safetyNotes = safetyNotes,
        script = script
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
    completed = completed,
    moodBefore = moodBefore,
    moodAfter = moodAfter,
    feedbackNote = feedbackNote,
    source = parsePracticeSessionSource(source),
    actualDurationSec = actualDurationSec,
    vibrationLevel = vibrationLevel,
    audioMode = audioMode?.let {
        runCatching { PracticeAudioMode.valueOf(it) }.getOrNull()
    },
    rating = rating
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
