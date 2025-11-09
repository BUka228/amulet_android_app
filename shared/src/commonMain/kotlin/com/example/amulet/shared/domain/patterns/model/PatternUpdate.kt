package com.example.amulet.shared.domain.patterns.model

/**
 * Обновления для существующего паттерна.
 */
data class PatternUpdate(
    val title: String? = null,
    val description: String? = null,
    val spec: PatternSpec? = null,
    val tags: List<String>? = null
)
