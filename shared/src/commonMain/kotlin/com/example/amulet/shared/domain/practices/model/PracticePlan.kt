package com.example.amulet.shared.domain.practices.model

import com.example.amulet.shared.domain.user.model.UserId

/** План / программа практик, привязанная к пользователю. */
data class PracticePlan(
    val id: String,
    val userId: UserId,
    val title: String,
    val description: String? = null,
    val status: String,
    val type: String? = null,
    val createdAt: Long,
    val updatedAt: Long? = null
)
