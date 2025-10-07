package com.example.amulet.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "privacy_jobs",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["type"]),
        Index(value = ["status"]),
        Index(value = ["expiresAt"])
    ]
)
data class PrivacyJobEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val type: String,
    val status: String,
    val payloadJson: String,
    val createdAt: Long,
    val updatedAt: Long,
    val expiresAt: Long?
)
