package com.example.amulet.core.network.dto.pair

import com.example.amulet.core.network.serialization.ApiTimestamp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PairDto(
    val id: String,
    val memberIds: List<String> = emptyList(),
    @SerialName("member_ids")
    val memberIdsSnake: List<String> = emptyList(),
    val status: String? = null,
    val createdAt: ApiTimestamp? = null,
    val blockedBy: String? = null,
    val blockedAt: ApiTimestamp? = null
)

@Serializable
data class PairResponseDto(
    val pair: PairDto
)

@Serializable
data class PairListResponseDto(
    val pairs: List<PairDto> = emptyList()
)

@Serializable
data class PairInviteRequestDto(
    val method: String,
    val userId: String? = null,
    val pairId: String? = null
)

@Serializable
data class PairInviteResponseDto(
    val pairId: String,
    val status: String
)

@Serializable
data class PairAcceptRequestDto(
    val pairId: String
)

@Serializable
data class PairShareResponseDto(
    val shared: Boolean? = null
)

@Serializable
data class PairEmotionDto(
    val id: String,
    @SerialName("pair_id")
    val pairId: String? = null,
    val name: String,
    @SerialName("color_hex")
    val colorHex: String,
    @SerialName("pattern_id")
    val patternId: String? = null,
    val order: Int
)

@Serializable
data class PairEmotionListResponseDto(
    val emotions: List<PairEmotionDto> = emptyList()
)

@Serializable
data class PairEmotionUpdateRequestDto(
    val emotions: List<PairEmotionDto>
)

@Serializable
data class PairMemberSettingsDto(
    val muted: Boolean,
    val quietHoursStartMinutes: Int? = null,
    val quietHoursEndMinutes: Int? = null,
    val maxHugsPerHour: Int? = null
)

@Serializable
data class PairMemberSettingsUpdateRequestDto(
    val settings: PairMemberSettingsDto
)

@Serializable
data class PairQuickReplyDto(
    val pairId: String,
    val userId: String,
    val gestureType: String,
    val emotionId: String?
)

@Serializable
data class PairQuickReplyListResponseDto(
    val replies: List<PairQuickReplyDto> = emptyList()
)

@Serializable
data class PairQuickReplyUpdateRequestDto(
    val replies: List<PairQuickReplyDto>
)
