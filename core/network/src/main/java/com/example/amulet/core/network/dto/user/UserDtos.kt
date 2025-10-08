package com.example.amulet.core.network.dto.user

import com.example.amulet.core.network.dto.common.CursorPageDto
import com.example.amulet.core.network.serialization.ApiTimestamp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class UserDto(
    val id: String,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val timezone: String? = null,
    val language: String? = null,
    val consents: JsonObject? = null,
    val createdAt: ApiTimestamp? = null,
    val updatedAt: ApiTimestamp? = null
)

@Serializable
data class UserResponseDto(
    val user: UserDto
)

@Serializable
data class UsersResponseDto(
    val users: List<UserDto>
)

@Serializable
data class UserInitRequestDto(
    val displayName: String? = null,
    val timezone: String? = null,
    val language: String? = null,
    val consents: JsonObject? = null
)

@Serializable
data class UserUpdateRequestDto(
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val timezone: String? = null,
    val language: String? = null,
    val consents: JsonObject? = null
)

@Serializable
data class PrivacyRightsResponseDto(
    val success: Boolean,
    val data: PrivacyRightsDataDto? = null
)

@Serializable
data class PrivacyRightsDataDto(
    val userId: String? = null,
    val rights: JsonObject? = null,
    val lastUpdated: ApiTimestamp? = null,
    val compliance: JsonObject? = null
)

@Serializable
data class UserListResponseDto(
    val data: CursorPageDto<UserDto>? = null
)
