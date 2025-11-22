package com.example.amulet.data.practices.seed

import com.example.amulet.shared.domain.practices.model.Practice
import com.example.amulet.shared.domain.practices.model.PracticeGoal
import com.example.amulet.shared.domain.practices.model.PracticeLevel
import com.example.amulet.shared.domain.practices.model.PracticeType

/**
 * Модель пресета для сидирования практик.
 */
data class PracticeSeed(
    val id: String,
    val type: PracticeType,
    val title: String,
    val description: String?,
    val durationSec: Int?,
    val level: PracticeLevel? = null,
    val goal: PracticeGoal? = null,
    val tags: List<String> = emptyList(),
    val contraindications: List<String> = emptyList(),
    val patternId: String?,
    val audioUrl: String?,
    val difficulty: String? = null,
    val category: String? = null,
    val usageCount: Int = 0,
    val createdAt: Long? = System.currentTimeMillis(),
    val updatedAt: Long? = System.currentTimeMillis(),
    val steps: List<String> = emptyList(),
    val safetyNotes: List<String> = emptyList()
)

/**
 * Конвертация из domain модели Practice в PracticeSeed
 */
fun Practice.toSeed(): PracticeSeed = PracticeSeed(
    id = id,
    type = type,
    title = title,
    description = description,
    durationSec = durationSec,
    level = level,
    goal = goal,
    tags = tags,
    contraindications = contraindications,
    patternId = patternId?.value,
    audioUrl = audioUrl,
    usageCount = usageCount,
    createdAt = createdAt,
    updatedAt = updatedAt,
    steps = steps,
    safetyNotes = safetyNotes
)
