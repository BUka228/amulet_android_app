package com.example.amulet.shared.domain.devices.model

/**
 * План команд для устройства.
 * Содержит последовательность команд и оценку длительности выполнения.
 */
data class AmuletCommandPlan(
    val commands: List<AmuletCommand>,
    val estimatedDurationMs: Long
)
