package com.example.amulet.core.network.dto.hug

import com.example.amulet.core.network.serialization.ApiTimestamp
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class HugDto(
    val id: String,
    val fromUserId: String? = null,
    val toUserId: String? = null,
    val pairId: String? = null,
    val emotion: HugEmotionDto? = null,
    val payload: JsonObject? = null,
    val inReplyToHugId: String? = null,
    val status: String? = null,
    val deliveredAt: ApiTimestamp? = null,
    val createdAt: ApiTimestamp? = null
)

@Serializable
data class HugEmotionDto(
    val color: String,
    val patternId: String? = null
)

@Serializable
data class HugSendRequestDto(
    val toUserId: String? = null,
    val pairId: String? = null,
    val emotion: HugEmotionDto,
    val payload: JsonObject? = null,
    val inReplyToHugId: String? = null
)

@Serializable
data class HugSendResponseDto(
    val hugId: String,
    val delivered: Boolean? = null
)

@Serializable
data class HugListResponseDto(
    val items: List<HugDto> = emptyList(),
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
