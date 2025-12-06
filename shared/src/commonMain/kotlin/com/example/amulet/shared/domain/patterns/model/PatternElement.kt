package com.example.amulet.shared.domain.patterns.model

import kotlinx.serialization.Serializable

@Serializable
data class PatternTimeline(
    val durationMs: Int,
    val tracks: List<TimelineTrack>
)

@Serializable
data class TimelineTrack(
    val target: TimelineTarget,
    val priority: Int = 0,
    val mixMode: MixMode = MixMode.OVERRIDE,
    val clips: List<TimelineClip>
)

@Serializable
sealed interface TimelineTarget

@Serializable
data class TargetLed(val index: Int) : TimelineTarget

@Serializable
data class TargetGroup(val indices: List<Int>) : TimelineTarget

@Serializable
data object TargetRing : TimelineTarget

@Serializable
data class TimelineClip(
    val startMs: Int,
    val durationMs: Int,
    val color: String,
    val fadeInMs: Int = 0,
    val fadeOutMs: Int = 0,
    val easing: Easing = Easing.LINEAR
)

@Serializable
enum class MixMode { OVERRIDE, ADDITIVE }

@Serializable
enum class Easing { LINEAR }
