@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.example.amulet.shared.domain.hugs.model

import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.user.model.UserId
import kotlinx.datetime.Instant
import kotlin.jvm.JvmInline

@JvmInline
value class HugId(val value: String)

@JvmInline
value class PairId(val value: String)

enum class HugStatus {
    SENT,
    DELIVERED,
    READ,
    EXPIRED
}

data class Emotion(
    val colorHex: String,
    val patternId: PatternId?
)

data class Hug(
    val id: HugId,
    val fromUserId: UserId,
    val toUserId: UserId?,
    val pairId: PairId?,
    val emotion: Emotion,
    val payload: Map<String, Any?>?,
    val inReplyToHugId: HugId?,
    val deliveredAt: Instant?,
    val createdAt: Instant,
    val status: HugStatus
)

data class PairMemberSettings(
    val muted: Boolean = false,
    val quietHoursStartMinutes: Int? = null,
    val quietHoursEndMinutes: Int? = null,
    val maxHugsPerHour: Int? = null
)

data class PairMember(
    val userId: UserId,
    val joinedAt: Instant,
    val settings: PairMemberSettings = PairMemberSettings()
)

enum class PairStatus {
    ACTIVE,
    PENDING,
    BLOCKED
}

data class Pair(
    val id: PairId,
    val members: List<PairMember>,
    val status: PairStatus,
    val blockedBy: UserId?,
    val blockedAt: Instant?,
    val createdAt: Instant
)

data class PairEmotion(
    val id: String,
    val pairId: PairId,
    val name: String,
    val colorHex: String,
    val patternId: PatternId?,
    val order: Int
)

enum class GestureType {
    DOUBLE_TAP,
    LONG_PRESS
}

data class PairQuickReply(
    val pairId: PairId,
    val userId: UserId,
    val gestureType: GestureType,
    val emotionId: String?
)
