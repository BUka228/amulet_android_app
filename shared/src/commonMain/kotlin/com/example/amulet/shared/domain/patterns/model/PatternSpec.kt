package com.example.amulet.shared.domain.patterns.model

import kotlinx.serialization.Serializable

/**
 * Спецификация паттерна.
 * Описывает анимацию через единый канонический таймлайн.
 */
@Serializable
data class PatternSpec(
    val type: String,
    val hardwareVersion: Int,
    val durationMs: Int? = null,
    val loop: Boolean = false,
    val timeline: PatternTimeline
)
