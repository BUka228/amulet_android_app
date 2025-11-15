package com.example.amulet.shared.domain.practices.model
data class PracticeFilter(
    val type: PracticeType? = null,
    val categoryId: String? = null,
    val tags: List<String> = emptyList(),
    val onlyFavorites: Boolean = false,
    val durationFromSec: Int? = null,
    val durationToSec: Int? = null
)
