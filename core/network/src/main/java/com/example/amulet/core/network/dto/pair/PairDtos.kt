package com.example.amulet.core.network.dto.pair

import com.example.amulet.core.network.serialization.ApiTimestamp
import kotlinx.serialization.Serializable

@Serializable
data class PairDto(
    val id: String,
    val memberIds: List<String> = emptyList(),
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
    val target: String? = null
)

@Serializable
data class PairInviteResponseDto(
    val inviteId: String,
    val url: String? = null
)

@Serializable
data class PairAcceptRequestDto(
    val inviteId: String
)

@Serializable
data class PairShareResponseDto(
    val shared: Boolean? = null
)
