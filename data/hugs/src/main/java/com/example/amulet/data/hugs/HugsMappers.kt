package com.example.amulet.data.hugs

import com.example.amulet.core.database.entity.HugEntity
import com.example.amulet.core.database.entity.PairEmotionEntity
import com.example.amulet.core.database.entity.PairMemberEntity
import com.example.amulet.core.database.entity.PairQuickReplyEntity
import com.example.amulet.core.database.relation.PairWithMemberSettings
import com.example.amulet.shared.domain.hugs.model.Emotion
import com.example.amulet.shared.domain.hugs.model.GestureType
import com.example.amulet.shared.domain.hugs.model.Hug
import com.example.amulet.shared.domain.hugs.model.HugId
import com.example.amulet.shared.domain.hugs.model.HugStatus
import com.example.amulet.shared.domain.hugs.model.Pair
import com.example.amulet.shared.domain.hugs.model.PairEmotion
import com.example.amulet.shared.domain.hugs.model.PairId
import com.example.amulet.shared.domain.hugs.model.PairMember
import com.example.amulet.shared.domain.hugs.model.PairMemberSettings
import com.example.amulet.shared.domain.hugs.model.PairQuickReply
import com.example.amulet.shared.domain.hugs.model.PairStatus
import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.user.model.UserId
import kotlinx.datetime.Instant

fun HugEntity.toDomainOrNull(): Hug? {
    val fromId = fromUserId ?: return null
    val emotion = Emotion(
        colorHex = emotionColor ?: "#FFFFFF",
        patternId = emotionPatternId?.let { PatternId(it) }
    )
    val statusEnum = when (status.uppercase()) {
        "SENT" -> HugStatus.SENT
        "DELIVERED" -> HugStatus.DELIVERED
        "READ" -> HugStatus.READ
        "EXPIRED" -> HugStatus.EXPIRED
        else -> HugStatus.SENT
    }

    return Hug(
        id = HugId(id),
        fromUserId = UserId(fromId),
        toUserId = toUserId?.let { UserId(it) },
        pairId = pairId?.let { PairId(it) },
        emotion = emotion,
        payload = payloadJson?.let { emptyMap<String, Any?>() }, // TODO: decode when формат будет определён
        inReplyToHugId = inReplyToHugId?.let { HugId(it) },
        deliveredAt = deliveredAt?.let { Instant.fromEpochMilliseconds(it) },
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        status = statusEnum
    )
}

fun PairWithMemberSettings.toDomain(): Pair = Pair(
    id = PairId(pair.id),
    members = members.map { it.toDomain() },
    status = when (pair.status.lowercase()) {
        "active" -> PairStatus.ACTIVE
        "pending" -> PairStatus.PENDING
        "blocked" -> PairStatus.BLOCKED
        else -> PairStatus.ACTIVE
    },
    blockedBy = pair.blockedBy?.let { UserId(it) },
    blockedAt = pair.blockedAt?.let { Instant.fromEpochMilliseconds(it) },
    createdAt = Instant.fromEpochMilliseconds(pair.createdAt)
)

fun PairMemberEntity.toDomain(): PairMember = PairMember(
    userId = UserId(userId),
    joinedAt = Instant.fromEpochMilliseconds(joinedAt),
    settings = PairMemberSettings(
        muted = muted,
        quietHoursStartMinutes = quietHoursStartMinutes,
        quietHoursEndMinutes = quietHoursEndMinutes,
        maxHugsPerHour = maxHugsPerHour
    )
)

fun PairEmotionEntity.toDomain(): PairEmotion = PairEmotion(
    id = id,
    pairId = PairId(pairId),
    name = name,
    colorHex = colorHex,
    patternId = patternId?.let { PatternId(it) },
    order = order
)

fun PairEmotion.toEntity(): PairEmotionEntity = PairEmotionEntity(
    id = id,
    pairId = pairId.value,
    name = name,
    colorHex = colorHex,
    patternId = patternId?.value,
    order = order
)

fun PairQuickReplyEntity.toDomain(): PairQuickReply = PairQuickReply(
    pairId = PairId(pairId),
    userId = UserId(userId),
    gestureType = when (gestureType.uppercase()) {
        "DOUBLE_TAP" -> GestureType.DOUBLE_TAP
        "LONG_PRESS" -> GestureType.LONG_PRESS
        else -> GestureType.DOUBLE_TAP
    },
    emotionId = emotionId
)

fun PairQuickReply.toEntity(): PairQuickReplyEntity = PairQuickReplyEntity(
    pairId = pairId.value,
    userId = userId.value,
    gestureType = gestureType.name,
    emotionId = emotionId
)
