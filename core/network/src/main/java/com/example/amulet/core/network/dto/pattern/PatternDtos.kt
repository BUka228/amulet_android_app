package com.example.amulet.core.network.dto.pattern

import com.example.amulet.core.network.serialization.ApiTimestamp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
data class PatternDto(
    val id: String,
    val version: Int,
    val ownerId: String? = null,
    val kind: String,
    val spec: PatternSpecDto,
    val public: Boolean? = null,
    val reviewStatus: String? = null,
    val hardwareVersion: Int? = null,
    val title: String? = null,
    val description: String? = null,
    val tags: List<String>? = null,
    val usageCount: Int? = null,
    val sharedWith: List<String>? = null,
    val createdAt: ApiTimestamp? = null,
    val updatedAt: ApiTimestamp? = null
)

@Serializable
data class PatternResponseDto(
    val pattern: PatternDto
)

@Serializable
data class PatternListResponseDto(
    val items: List<PatternDto> = emptyList(),
    val nextCursor: String? = null
)

@Serializable
data class PatternCreateRequestDto(
    val kind: String,
    val spec: PatternSpecDto,
    val title: String? = null,
    val description: String? = null,
    val tags: List<String>? = null,
    val public: Boolean? = null,
    val hardwareVersion: Int
)

@Serializable
data class PatternUpdateRequestDto(
    val version: Int,
    val kind: String? = null,
    val spec: PatternSpecDto? = null,
    val title: String? = null,
    val description: String? = null,
    val tags: List<String>? = null,
    val public: Boolean? = null
)

@Serializable
data class PatternShareRequestDto(
    val toUserId: String? = null,
    val pairId: String? = null
)

@Serializable
data class PatternShareResponseDto(
    val shared: Boolean? = null
)

@Serializable
data class PatternPreviewRequestDto(
    val deviceId: String,
    val spec: PatternSpecDto,
    val duration: Int? = null
)

@Serializable
data class PatternPreviewResponseDto(
    val previewId: String
)

@Serializable
data class PatternDeleteResponseDto(
    val ok: Boolean
)

@Serializable
data class PatternSpecDto(
    val type: String,
    val hardwareVersion: Int,
    val duration: Int? = null,
    val loop: Boolean? = null,
    val timeline: PatternTimelineDto
)

@Serializable
data class PatternTimelineDto(
    val durationMs: Int,
    val tracks: List<TimelineTrackDto>
)

@Serializable
data class TimelineTrackDto(
    val target: TimelineTargetDto,
    val priority: Int = 0,
    val mixMode: MixModeDto = MixModeDto.OVERRIDE,
    val clips: List<TimelineClipDto>
)

@Serializable
@JsonClassDiscriminator("type")
sealed class TimelineTargetDto

@Serializable
@SerialName("led")
data class TargetLedDto(val index: Int) : TimelineTargetDto()

@Serializable
@SerialName("group")
data class TargetGroupDto(val indices: List<Int>) : TimelineTargetDto()

@Serializable
@SerialName("ring")
object TargetRingDto : TimelineTargetDto()

@Serializable
data class TimelineClipDto(
    val startMs: Int,
    val durationMs: Int,
    val color: String,
    val fadeInMs: Int = 0,
    val fadeOutMs: Int = 0,
    val easing: EasingDto = EasingDto.LINEAR
)

@Serializable
enum class MixModeDto { OVERRIDE, ADDITIVE }

@Serializable
enum class EasingDto { LINEAR }
