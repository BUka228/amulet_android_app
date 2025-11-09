package com.example.amulet.shared.domain.patterns.model

/**
 * Метаданные для публикации паттерна.
 */
data class PublishMetadata(
    val title: String,
    val description: String,
    val tags: List<String>
)
