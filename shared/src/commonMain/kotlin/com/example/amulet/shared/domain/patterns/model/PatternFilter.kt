package com.example.amulet.shared.domain.patterns.model

/**
 * Фильтр для поиска паттернов.
 */
data class PatternFilter(
    val kind: PatternKind? = null,
    val hardwareVersion: Int? = null,
    val tags: List<String> = emptyList(),
    val query: String? = null,
    val publicOnly: Boolean = false
)
