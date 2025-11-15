package com.example.amulet.shared.domain.practices.model

data class UserPreferences(
    val defaultIntensity: Double? = null,
    val defaultBrightness: Double? = null,
    val goals: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    val preferredDurationsSec: List<Int> = emptyList()
)
