package com.example.amulet.shared.domain.patterns.compiler

import com.example.amulet.shared.domain.devices.model.AmuletCommandPlan
import com.example.amulet.shared.domain.patterns.model.PatternSpec

/**
 * Компилятор паттернов.
 * Преобразует высокоуровневую спецификацию паттерна в последовательность команд для устройства.
 */
interface PatternCompiler {
    fun compile(
        spec: PatternSpec,
        hardwareVersion: Int,
        firmwareVersion: String,
        intensity: Double = 1.0
    ): AmuletCommandPlan
}
