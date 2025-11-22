package com.example.amulet.shared.domain.practices.model

data class PracticeSchedule(
    val id: String,
    val userId: String,
    val practiceId: PracticeId,
    val courseId: String? = null,
    val daysOfWeek: List<Int>, // 1=Mon, 7=Sun
    val timeOfDay: String, // HH:mm
    val reminderEnabled: Boolean,
    val createdAt: Long
)
