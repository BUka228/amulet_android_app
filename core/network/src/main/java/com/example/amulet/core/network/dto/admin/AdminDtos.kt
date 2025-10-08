package com.example.amulet.core.network.dto.admin

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class AdminStatsOverviewResponseDto(
    val users: JsonObject? = null,
    val devices: JsonObject? = null,
    val patterns: JsonObject? = null,
    val practices: JsonObject? = null,
    val firmware: JsonObject? = null,
    val activity: JsonObject? = null,
    val overview: JsonObject? = null,
    val lastUpdated: JsonObject? = null,
    val aggregationPeriod: String? = null,
    val nextUpdate: JsonObject? = null
)

@Serializable
data class AdminUserRolesResponseDto(
    val uid: String,
    val roles: Map<String, Boolean> = emptyMap()
)
