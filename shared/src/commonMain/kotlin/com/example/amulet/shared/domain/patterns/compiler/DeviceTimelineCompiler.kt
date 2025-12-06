package com.example.amulet.shared.domain.patterns.compiler

import com.example.amulet.shared.domain.devices.model.DeviceTimelineSegment
import com.example.amulet.shared.domain.patterns.model.PatternTimeline

/**
 * Компилятор таймлайна паттерна в устройство-специфичные сегменты.
 */
interface DeviceTimelineCompiler {
    fun compile(
        timeline: PatternTimeline,
        hardwareVersion: Int,
        firmwareVersion: String,
        intensity: Double = 1.0
    ): List<DeviceTimelineSegment>
}
