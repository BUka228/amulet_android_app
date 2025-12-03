package com.example.amulet.shared.domain.user.model

import com.example.amulet.shared.domain.practices.model.PracticeAudioMode

data class UserPreferences(
    val defaultIntensity: Double? = null,
    val defaultBrightness: Double? = null,
    val defaultAudioMode: PracticeAudioMode? = null,
    val goals: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    val preferredDurationsSec: List<Int> = emptyList(),
    val hugsDndEnabled: Boolean = false,
)
