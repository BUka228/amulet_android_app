package com.example.amulet.shared.domain.practices.model

import com.example.amulet.shared.domain.user.model.UserId

data class PracticeStatistics(
    val userId: UserId,
    val totalSessions: Int = 0,
    val totalDurationSec: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val sessionsByType: Map<PracticeType, Int> = emptyMap(),
    val sessionsByGoal: Map<PracticeGoal, Int> = emptyMap(),
    val completionRate: Float = 0f, // 0.0 - 1.0
    val lastSessionAt: Long?,
    val updatedAt: Long
)
