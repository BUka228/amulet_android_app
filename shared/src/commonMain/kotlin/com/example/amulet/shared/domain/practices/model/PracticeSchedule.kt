package com.example.amulet.shared.domain.practices.model

import com.example.amulet.shared.domain.user.model.UserId

data class PracticeSchedule(
    val id: String,
    val userId: UserId,
    val practiceId: PracticeId,
    val daysOfWeek: Set<Int>, // 1-7 (Monday-Sunday)
    val timeOfDay: String, // HH:mm format
    val reminderEnabled: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long?
)
