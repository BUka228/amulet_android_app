package com.example.amulet.data.patterns.mapper

import com.example.amulet.core.database.entity.PatternEntity
import com.example.amulet.core.database.entity.PatternShareEntity
import com.example.amulet.core.database.entity.TagEntity
import com.example.amulet.core.network.dto.pattern.PatternDto
import com.example.amulet.core.network.dto.pattern.PatternSpecDto
import com.example.amulet.shared.domain.patterns.model.Pattern
import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.patterns.model.PatternKind
import com.example.amulet.shared.domain.patterns.model.PatternSpec
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
        updatedAt = updatedAt
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
        updatedAt = updatedAt
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
        durationMs = duration,
        loop = loop ?: false,
        elements = emptyList() // Элементы маппятся отдельно при необходимости
    )
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
