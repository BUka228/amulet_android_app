package com.example.amulet.shared.domain.courses.model

data class Course(
    val id: CourseId,
    val title: String,
    val description: String?,
    val tags: List<String> = emptyList(),
    val totalDurationSec: Int? = null,
    val difficulty: String? = null,
    val coverUrl: String? = null,
    val createdAt: Long?,
    val updatedAt: Long?
)
