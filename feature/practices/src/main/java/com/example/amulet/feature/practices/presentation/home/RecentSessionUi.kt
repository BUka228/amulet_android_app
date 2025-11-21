package com.example.amulet.feature.practices.presentation.home

data class RecentSessionUi(
    val id: String,
    val practiceId: String,
    val practiceTitle: String,
    val durationSec: Int?
)
