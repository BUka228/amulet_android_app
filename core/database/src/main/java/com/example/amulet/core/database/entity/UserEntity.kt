package com.example.amulet.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.amulet.shared.domain.privacy.model.UserConsents
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Entity(
    tableName = "users",
    indices = [Index(value = ["updatedAt"])]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val displayName: String?,
    val avatarUrl: String?,
    val timezone: String?,
    val language: String?,
    val consents: UserConsents?,
    val createdAt: Instant?,
    val updatedAt: Instant?
)
