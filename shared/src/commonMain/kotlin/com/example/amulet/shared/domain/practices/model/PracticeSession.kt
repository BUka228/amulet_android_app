package com.example.amulet.shared.domain.practices.model

import com.example.amulet.shared.domain.devices.model.DeviceId
import com.example.amulet.shared.domain.user.model.UserId

enum class PracticeAudioMode {
    GUIDE,
    SOUND_ONLY,
    SILENT
}

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
    val completed: Boolean,
    val moodBefore: Int? = null,
    val moodAfter: Int? = null,
    val feedbackNote: String? = null,
    val source: PracticeSessionSource? = null,
    val actualDurationSec: Int? = null,
    val vibrationLevel: Double? = null,
    val audioMode: PracticeAudioMode? = null,
    val rating: Int? = null
)
