package com.example.amulet.shared.domain.practices.model

import com.example.amulet.shared.domain.user.model.UserId

/** Достижение пользователя по практикам. */
data class PracticeBadge(
    val id: String,
    val userId: UserId,
    val code: String,
    val earnedAt: Long,
    val metadata: Map<String, String> = emptyMap()
)
