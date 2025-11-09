package com.example.amulet.shared.domain.patterns.model

import kotlinx.serialization.Serializable

/**
 * Спецификация паттерна.
 * Содержит последовательность элементов анимации и параметры воспроизведения.
 */
@Serializable
data class PatternSpec(
    val type: String,
    val hardwareVersion: Int,
    val durationMs: Int? = null,
    val loop: Boolean = false,
    val elements: List<PatternElement> = emptyList()
)
