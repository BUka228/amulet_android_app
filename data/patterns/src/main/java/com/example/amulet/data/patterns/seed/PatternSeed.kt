package com.example.amulet.data.patterns.seed

import com.example.amulet.shared.domain.patterns.model.PatternSpec

/**
 * Модель пресета для сидирования паттернов.
 */
data class PatternSeed(
    val id: String,
    val title: String,
    val description: String?,
    val kind: String,
    val spec: PatternSpec,
    val public: Boolean,
    val tags: List<String> = emptyList(),
    val ownerId: String? = null,
    val sharedWith: List<String> = emptyList(),
    val version: Int = 1,
    val createdAt: Long? = System.currentTimeMillis(),
    val updatedAt: Long? = System.currentTimeMillis()
) {
    val specJson: String get() = com.example.amulet.shared.domain.patterns.builder.PatternJson.encode(spec)
}
