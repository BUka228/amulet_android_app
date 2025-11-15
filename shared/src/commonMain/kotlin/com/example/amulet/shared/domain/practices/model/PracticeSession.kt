package com.example.amulet.shared.domain.practices.model

import com.example.amulet.shared.domain.devices.model.DeviceId
import com.example.amulet.shared.domain.user.model.UserId
data class PracticeSession(
    val id: PracticeSessionId,
    val userId: UserId,
    val practiceId: PracticeId,
    val deviceId: DeviceId?,
    val status: PracticeSessionStatus,
    val startedAt: Long,
    val completedAt: Long?,
    val durationSec: Int?,
    val intensity: Double?,
    val brightness: Double?,
    val completed: Boolean
)
