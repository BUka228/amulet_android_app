package com.example.amulet.shared.domain.patterns.model

import com.example.amulet.shared.domain.user.model.UserId

/**
 * Основная доменная модель паттерна.
 * Паттерн - это световая и/или вибрационная анимация для амулета.
 */
data class Pattern(
    val id: PatternId,
    val version: Int,  // Для оптимистической блокировки
    val ownerId: UserId?,  // null для системных паттернов
    val kind: PatternKind,
    val spec: PatternSpec,
    val public: Boolean,
    val reviewStatus: ReviewStatus?,
    val hardwareVersion: Int,
    val title: String,
    val description: String?,
    val tags: List<String> = emptyList(),
    val usageCount: Int? = null,
    val sharedWith: List<UserId> = emptyList(),
    val createdAt: Long?,
    val updatedAt: Long?,
    val parentPatternId: PatternId? = null,
    val segmentIndex: Int? = null,
    val segmentStartMs: Int? = null,
    val segmentEndMs: Int? = null,
)
