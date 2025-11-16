package com.example.amulet.data.practices.seed

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
    val patternId: String?,
    val audioUrl: String?,
    val tags: List<String> = emptyList(),
    val difficulty: String? = null,
    val category: String? = null,
    val createdAt: Long? = System.currentTimeMillis(),
    val updatedAt: Long? = System.currentTimeMillis()
)
