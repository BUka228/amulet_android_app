package com.example.amulet.data.practices.mapper

import com.example.amulet.core.database.entity.PracticeCategoryEntity
import com.example.amulet.core.database.entity.PracticeEntity
import com.example.amulet.data.practices.seed.PracticeSeed
import com.example.amulet.shared.domain.practices.model.PracticeType

/**
 * Мапперы для преобразования PracticeSeed в Entity.
 */

fun PracticeSeed.toEntity(): PracticeEntity = PracticeEntity(
    id = id,
    type = type.name,
    title = title,
    description = description,
    durationSec = durationSec,
    patternId = patternId?.toString(),
    audioUrl = audioUrl,
    localesJson = "[]", // Пустой JSON массив для локалей
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun PracticeSeed.toCategoryEntity(): PracticeCategoryEntity = PracticeCategoryEntity(
    id = category?.lowercase()?.replace(" ", "_") ?: "other",
    title = category ?: "Другое",
    order = 0
)
