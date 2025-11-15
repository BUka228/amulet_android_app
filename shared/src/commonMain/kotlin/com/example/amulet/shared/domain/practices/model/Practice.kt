package com.example.amulet.shared.domain.practices.model

import com.example.amulet.shared.domain.patterns.model.PatternId
data class Practice(
    val id: PracticeId,
    val type: PracticeType,
    val title: String,
    val description: String?,
    val durationSec: Int?,
    val patternId: PatternId?,
    val audioUrl: String?,
    val createdAt: Long?,
    val updatedAt: Long?
)
