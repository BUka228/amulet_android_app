package com.example.amulet.shared.domain.practices.model

import com.example.amulet.shared.domain.patterns.model.PatternId

data class Practice(
    val id: PracticeId,
    val type: PracticeType,
    val title: String,
    val description: String?,
    val durationSec: Int?,
    val level: PracticeLevel?,
    val goal: PracticeGoal?,
    val tags: List<String> = emptyList(),
    val contraindications: List<String> = emptyList(),
    val patternId: PatternId?,
    val audioUrl: String?,
    val isFavorite: Boolean = false,
    val usageCount: Int = 0,
    val createdAt: Long?,
    val updatedAt: Long?,
    val steps: List<String> = emptyList(),
    val safetyNotes: List<String> = emptyList(),
    val script: PracticeScript? = null,
    val hasDeviceScript: Boolean = false,
)
