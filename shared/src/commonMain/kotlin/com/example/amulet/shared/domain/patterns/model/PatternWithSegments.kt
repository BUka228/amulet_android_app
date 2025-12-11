package com.example.amulet.shared.domain.patterns.model

/**
 * Композитная модель для редактора: базовый паттерн + его сегменты.
 */
data class PatternWithSegments(
    val base: Pattern,
    val segments: List<Pattern>,
)
