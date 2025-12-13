package com.example.amulet.core.network.dto.hug

import com.example.amulet.core.network.serialization.ApiTimestamp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class HugDto(
    val id: String,
    @SerialName("from_user_id")
    val fromUserId: String? = null,
    @SerialName("to_user_id")
    val toUserId: String? = null,
    @SerialName("pair_id")
    val pairId: String? = null,
    val emotion: HugEmotionDto? = null,
    val payload: JsonObject? = null,
    @SerialName("in_reply_to_hug_id")
    val inReplyToHugId: String? = null,
    val status: String? = null,
    @SerialName("delivered_at")
    val deliveredAt: ApiTimestamp? = null,
    @SerialName("created_at")
    val createdAt: ApiTimestamp? = null
)

@Serializable
data class HugEmotionDto(
    val color: String,
    @SerialName("pattern_id")
    val patternId: String? = null
)

@Serializable
data class HugSendRequestDto(
    @SerialName("to_user_id")
    val toUserId: String? = null,
    @SerialName("pair_id")
    val pairId: String? = null,
    val emotion: HugEmotionDto,
    val payload: JsonObject? = null,
    @SerialName("in_reply_to_hug_id")
    val inReplyToHugId: String? = null
)

@Serializable
data class HugSendResponseDto(
    @SerialName("hug_id")
    val hugId: String,
    val delivered: Boolean? = null
)

@Serializable
data class HugListResponseDto(
    val items: List<HugDto> = emptyList(),
    @SerialName("next_cursor")
    val nextCursor: String? = null
)

@Serializable
data class HugResponseDto(
    val hug: HugDto
)

@Serializable
data class HugStatusUpdateRequestDto(
    val status: String
)
