package com.example.amulet.shared.domain.practices.model

data class UserPreferences(
    val defaultIntensity: Double? = null,
    val defaultBrightness: Double? = null,
    val defaultAudioMode: PracticeAudioMode? = null,
    val goals: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    val preferredDurationsSec: List<Int> = emptyList(),
    /**
     * Глобальный режим DND для «объятий».
     * При включении амулет не будет реагировать на входящие объятия, но пуши останутся.
     */
    val hugsDndEnabled: Boolean = false,
)
