package com.example.amulet.shared.domain.patterns.model

/**
 * Редакторская модель маркеров таймлайна паттерна.
 */
data class PatternMarkers(
    val patternId: PatternId,
    val markersMs: List<Int>,
)
