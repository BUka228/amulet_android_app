package com.example.amulet.core.network.dto.common

import com.example.amulet.core.network.serialization.ApiTimestamp
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class CursorPageDto<T>(
    val items: List<T>,
    val nextCursor: String? = null
)

@Serializable
data class ErrorDto(
    val code: String? = null,
    val message: String? = null,
    val details: JsonObject? = null
)

@Serializable
data class JobStatusDto(
    val jobId: String,
    val status: String,
    val createdAt: ApiTimestamp? = null,
    val downloadUrl: String? = null,
    val fileSize: Long? = null,
    val errorMessage: String? = null,
    val expiresAt: ApiTimestamp? = null
)
