package com.example.amulet.core.network.dto.practice

import com.example.amulet.core.network.serialization.ApiTimestamp
import kotlinx.serialization.Serializable

@Serializable
data class PracticeDto(
    val id: String,
    val type: String? = null,
    val title: String? = null,
    val desc: String? = null,
    val durationSec: Int? = null,
    val patternId: String? = null,
    val audioUrl: String? = null,
    val locales: Map<String, String>? = null
)

@Serializable
data class PracticeResponseDto(
    val practice: PracticeDto
)

@Serializable
data class PracticeListResponseDto(
    val items: List<PracticeDto> = emptyList(),
    val nextCursor: String? = null
)

@Serializable
data class PracticeStartRequestDto(
    val deviceId: String? = null,
    val intensity: Double? = null,
    val brightness: Double? = null
)

@Serializable
data class PracticeStopRequestDto(
    val completed: Boolean,
    val durationSec: Int? = null
)

@Serializable
data class PracticeSummaryDto(
    val durationSec: Int? = null,
    val completed: Boolean? = null,
    val createdAt: ApiTimestamp? = null
)
