package com.example.amulet.shared.domain.practices.model

import com.example.amulet.shared.domain.patterns.model.PatternId
import kotlinx.serialization.Serializable

@Serializable
enum class PracticeStepType {
    BREATH_STEP,
    TEXT_HINT,
    SILENCE,
    SOUND_SCAPE,
    BODY_SCAN,
    CUSTOM
}

@Serializable
data class PracticeStep(
    val order: Int,
    val type: PracticeStepType,
    val title: String? = null,
    val description: String? = null,
    val durationSec: Int? = null,
    val patternId: String? = null,
    val audioUrl: String? = null,
    val extra: Map<String, String> = emptyMap()
)

@Serializable
data class PracticeScript(
    val steps: List<PracticeStep> = emptyList()
)
