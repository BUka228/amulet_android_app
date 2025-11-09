package com.example.amulet.shared.domain.patterns.model

/**
 * Черновик паттерна для создания.
 */
data class PatternDraft(
    val kind: PatternKind,
    val spec: PatternSpec,
    val hardwareVersion: Int,
    val title: String,
    val description: String?
)
