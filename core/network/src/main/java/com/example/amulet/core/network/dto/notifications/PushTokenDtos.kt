package com.example.amulet.core.network.dto.notifications

import com.example.amulet.core.network.serialization.ApiTimestamp
import kotlinx.serialization.Serializable

@Serializable
data class UserPushTokenDto(
    val id: String,
    val token: String,
    val platform: String? = null,
    val createdAt: ApiTimestamp? = null,
    val lastSeenAt: ApiTimestamp? = null
)

@Serializable
data class UserPushTokensResponseDto(
    val success: Boolean,
    val tokens: List<UserPushTokenDto> = emptyList()
)

@Serializable
data class UserPushTokenRequestDto(
    val token: String,
    val platform: String? = null,
    val lastSeenAt: ApiTimestamp? = null
)
