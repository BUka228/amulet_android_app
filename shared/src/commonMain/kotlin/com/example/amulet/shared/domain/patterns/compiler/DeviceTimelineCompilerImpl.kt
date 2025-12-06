package com.example.amulet.shared.domain.patterns.compiler

import com.example.amulet.shared.core.logging.Logger
import com.example.amulet.shared.domain.devices.model.DeviceTimelineSegment
import com.example.amulet.shared.domain.devices.model.Rgb
import com.example.amulet.shared.domain.patterns.model.*
import kotlin.math.roundToInt

/**
 * Реализация DeviceTimelineCompiler: PatternTimeline -> список DeviceTimelineSegment.
 */
class DeviceTimelineCompilerImpl : DeviceTimelineCompiler {

    override fun compile(
        timeline: PatternTimeline,
        hardwareVersion: Int,
        firmwareVersion: String,
        intensity: Double
    ): List<DeviceTimelineSegment> {
        val segments = mutableListOf<DeviceTimelineSegment>()

        timeline.tracks.forEach { track ->
            val targetMask = targetToMask(track.target)
            val priority = track.priority.coerceIn(0, 255)

            track.clips.forEach { clip ->
                val baseColor = Rgb.fromHex(clip.color)
                val scaledColor = applyIntensity(baseColor, intensity)

                val start = clip.startMs.toLong().coerceAtLeast(0L)
                val duration = clip.durationMs.toLong().coerceAtLeast(0L)

                val fadeIn = clip.fadeInMs.coerceAtLeast(0)
                val fadeOut = clip.fadeOutMs.coerceAtLeast(0)

                val easing = clip.easing

                segments += DeviceTimelineSegment(
                    targetMask = targetMask,
                    priority = priority,
                    mixMode = track.mixMode,
                    startMs = start,
                    durationMs = duration,
                    fadeInMs = fadeIn,
                    fadeOutMs = fadeOut,
                    easingIn = easing,
                    easingOut = easing,
                    color = scaledColor
                )
            }
        }

        if (segments.size > MAX_SEGMENTS_PER_PLAN) {
            Logger.w(
                "DeviceTimelineCompiler: segments count=${segments.size} exceeds max=$MAX_SEGMENTS_PER_PLAN, trimming",
                tag = TAG
            )
            return segments.take(MAX_SEGMENTS_PER_PLAN)
        }

        return segments
    }

    private fun targetToMask(target: TimelineTarget, leds: Int = 8): Int {
        return when (target) {
            is TargetLed -> if (target.index in 0 until leds) (1 shl target.index) else 0
            is TargetGroup -> target.indices.fold(0) { acc, idx ->
                if (idx in 0 until leds) acc or (1 shl idx) else acc
            }
            is TargetRing -> (1 shl leds) - 1 // все 8 диодов
        }
    }

    private fun applyIntensity(color: Rgb, intensity: Double): Rgb {
        val k = intensity.coerceIn(0.0, 1.0)
        if (k >= 0.999) return color
        fun scale(c: Int): Int = (c * k).roundToInt().coerceIn(0, 255)
        return Rgb(
            red = scale(color.red),
            green = scale(color.green),
            blue = scale(color.blue)
        )
    }

    private companion object {
        private const val TAG = "DeviceTimelineCompiler"
        private const val MAX_SEGMENTS_PER_PLAN = 512
    }
}
