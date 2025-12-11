package com.example.amulet.data.patterns.mapper

import com.example.amulet.core.database.entity.PatternEntity
import com.example.amulet.core.database.entity.PatternShareEntity
import com.example.amulet.core.database.entity.TagEntity
import com.example.amulet.core.network.dto.pattern.PatternDto
import com.example.amulet.core.network.dto.pattern.PatternSpecDto
import com.example.amulet.core.network.dto.pattern.PatternTimelineDto
import com.example.amulet.core.network.dto.pattern.TimelineTrackDto
import com.example.amulet.core.network.dto.pattern.TimelineClipDto
import com.example.amulet.core.network.dto.pattern.TimelineTargetDto
import com.example.amulet.core.network.dto.pattern.TargetLedDto
import com.example.amulet.core.network.dto.pattern.TargetGroupDto
import com.example.amulet.core.network.dto.pattern.TargetRingDto
import com.example.amulet.core.network.dto.pattern.MixModeDto
import com.example.amulet.core.network.dto.pattern.EasingDto
import com.example.amulet.shared.domain.patterns.model.Pattern
import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.patterns.model.PatternKind
import com.example.amulet.shared.domain.patterns.model.PatternSpec
import com.example.amulet.shared.domain.patterns.model.PatternTimeline
import com.example.amulet.shared.domain.patterns.model.TimelineTrack
import com.example.amulet.shared.domain.patterns.model.TimelineClip
import com.example.amulet.shared.domain.patterns.model.TimelineTarget
import com.example.amulet.shared.domain.patterns.model.TargetLed
import com.example.amulet.shared.domain.patterns.model.TargetGroup
import com.example.amulet.shared.domain.patterns.model.TargetRing
import com.example.amulet.shared.domain.patterns.model.MixMode
import com.example.amulet.shared.domain.patterns.model.Easing
import com.example.amulet.shared.domain.patterns.model.ReviewStatus
import com.example.amulet.shared.domain.user.model.UserId
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * Маппер для преобразования между Entity, Dto и Domain моделями паттернов.
 */

// ===== Entity -> Domain =====

@OptIn(kotlin.time.ExperimentalTime::class)
fun PatternEntity.toDomain(
    tags: List<String> = emptyList(),
    sharedWith: List<String> = emptyList()
): Pattern {
    val json = Json { ignoreUnknownKeys = true }
    return Pattern(
        id = PatternId(id),
        version = version,
        ownerId = ownerId?.let { UserId(it) },
        kind = PatternKind.valueOf(kind.uppercase()),
        spec = json.decodeFromString<PatternSpec>(specJson),
        public = public,
        reviewStatus = reviewStatus?.let { ReviewStatus.valueOf(it.uppercase()) },
        hardwareVersion = hardwareVersion,
        title = title,
        description = description,
        tags = tags,
        usageCount = usageCount,
        sharedWith = sharedWith.map { UserId(it) },
        createdAt = createdAt,
        updatedAt = updatedAt,
        parentPatternId = parentPatternId?.let(::PatternId),
        segmentIndex = segmentIndex,
        segmentStartMs = segmentStartMs,
        segmentEndMs = segmentEndMs,
    )
}

// ===== Domain -> Entity =====

@OptIn(kotlin.time.ExperimentalTime::class)
fun Pattern.toEntity(): PatternEntity {
    val json = Json { ignoreUnknownKeys = true }
    return PatternEntity(
        id = id.value,
        ownerId = ownerId?.value,
        kind = kind.name.lowercase(),
        hardwareVersion = hardwareVersion,
        title = title,
        description = description,
        specJson = json.encodeToString(spec),
        public = public,
        reviewStatus = reviewStatus?.name?.lowercase(),
        usageCount = usageCount,
        version = version,
        createdAt = createdAt,
        updatedAt = updatedAt,
        parentPatternId = parentPatternId?.value,
        segmentIndex = segmentIndex,
        segmentStartMs = segmentStartMs,
        segmentEndMs = segmentEndMs,
    )
}

// ===== Dto -> Domain =====

@OptIn(kotlin.time.ExperimentalTime::class)
fun PatternDto.toDomain(): Pattern {
    return Pattern(
        id = PatternId(id),
        version = version,
        ownerId = ownerId?.let { UserId(it) },
        kind = PatternKind.valueOf(kind.uppercase()),
        spec = spec.toDomain(),
        public = public ?: false,
        reviewStatus = reviewStatus?.let { ReviewStatus.valueOf(it.uppercase()) },
        hardwareVersion = hardwareVersion ?: 1,
        title = title ?: "",
        description = description,
        tags = tags ?: emptyList(),
        usageCount = usageCount,
        sharedWith = sharedWith?.map { UserId(it) } ?: emptyList(),
        createdAt = createdAt?.value,
        updatedAt = updatedAt?.value
    )
}

fun PatternSpecDto.toDomain(): PatternSpec {
    return PatternSpec(
        type = type,
        hardwareVersion = hardwareVersion,
        durationMs = duration ?: timeline.durationMs,
        loop = loop ?: false,
        timeline = timeline.toDomain()
    )
}

private fun PatternTimelineDto.toDomain(): PatternTimeline = PatternTimeline(
    durationMs = durationMs,
    tracks = tracks.map { it.toDomain() }
)

private fun TimelineTrackDto.toDomain(): TimelineTrack = TimelineTrack(
    target = target.toDomain(),
    priority = priority,
    mixMode = mixMode.toDomain(),
    clips = clips.map { it.toDomain() }
)

private fun TimelineClipDto.toDomain(): TimelineClip = TimelineClip(
    startMs = startMs,
    durationMs = durationMs,
    color = color,
    fadeInMs = fadeInMs,
    fadeOutMs = fadeOutMs,
    easing = easing.toDomain()
)

private fun TimelineTargetDto.toDomain(): TimelineTarget = when (this) {
    is TargetLedDto -> TargetLed(index)
    is TargetGroupDto -> TargetGroup(indices)
    is TargetRingDto -> TargetRing
}

private fun MixModeDto.toDomain(): MixMode = when (this) {
    MixModeDto.OVERRIDE -> MixMode.OVERRIDE
    MixModeDto.ADDITIVE -> MixMode.ADDITIVE
}

private fun EasingDto.toDomain(): Easing = when (this) {
    EasingDto.LINEAR -> Easing.LINEAR
}

// ===== Domain -> DTO =====

fun PatternSpec.toDto(): PatternSpecDto = PatternSpecDto(
    type = type,
    hardwareVersion = hardwareVersion,
    duration = durationMs,
    loop = loop,
    timeline = timeline.toDto()
)

private fun PatternTimeline.toDto(): PatternTimelineDto = PatternTimelineDto(
    durationMs = durationMs,
    tracks = tracks.map { it.toDto() }
)

private fun TimelineTrack.toDto(): TimelineTrackDto = TimelineTrackDto(
    target = target.toDto(),
    priority = priority,
    mixMode = mixMode.toDto(),
    clips = clips.map { it.toDto() }
)

private fun TimelineClip.toDto(): TimelineClipDto = TimelineClipDto(
    startMs = startMs,
    durationMs = durationMs,
    color = color,
    fadeInMs = fadeInMs,
    fadeOutMs = fadeOutMs,
    easing = easing.toDto()
)

private fun TimelineTarget.toDto(): TimelineTargetDto = when (this) {
    is TargetLed -> TargetLedDto(index)
    is TargetGroup -> TargetGroupDto(indices)
    is TargetRing -> TargetRingDto
}

private fun MixMode.toDto(): MixModeDto = when (this) {
    MixMode.OVERRIDE -> MixModeDto.OVERRIDE
    MixMode.ADDITIVE -> MixModeDto.ADDITIVE
}

private fun Easing.toDto(): EasingDto = when (this) {
    Easing.LINEAR -> EasingDto.LINEAR
}

// ===== Dto -> Entity =====

fun PatternDto.toEntity(): PatternEntity {
    val json = Json { ignoreUnknownKeys = true }
    return PatternEntity(
        id = id,
        ownerId = ownerId,
        kind = kind.lowercase(),
        hardwareVersion = hardwareVersion ?: 1,
        title = title ?: "",
        description = description,
        specJson = json.encodeToString(spec),
        public = public ?: false,
        reviewStatus = reviewStatus?.lowercase(),
        usageCount = usageCount,
        version = version,
        createdAt = createdAt?.value,
        updatedAt = updatedAt?.value
    )
}

// ===== Вспомогательные функции =====

fun List<TagEntity>.toTagNames(): List<String> {
    return map { it.name }
}

fun List<PatternShareEntity>.toUserIds(): List<String> {
    return map { it.userId }
}

fun List<String>.toTagEntities(): List<TagEntity> {
    return map { tagName ->
        TagEntity(
            id = UUID.randomUUID().toString(),
            name = tagName
        )
    }
}
