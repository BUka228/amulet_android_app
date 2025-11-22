package com.example.amulet.shared.domain.practices.model

import com.example.amulet.shared.domain.courses.model.CourseId

data class ScheduledSession(
    val id: String,
    val practiceId: String,
    val practiceTitle: String,
    val courseId: String? = null,
    val scheduledTime: Long,
    val status: ScheduledSessionStatus = ScheduledSessionStatus.PLANNED,
    val durationSec: Int? = null
)

enum class ScheduledSessionStatus {
    PLANNED,
    MISSED,
    COMPLETED
}
