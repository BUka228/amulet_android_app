package com.example.amulet.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "practices",
    foreignKeys = [
        ForeignKey(
            entity = PatternEntity::class,
            parentColumns = ["id"],
            childColumns = ["patternId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["type"]),
        Index(value = ["durationSec"]),
        Index(value = ["patternId"]),
        Index(value = ["level"]),
        Index(value = ["goal"])
    ]
)
data class PracticeEntity(
    @PrimaryKey val id: String,
    val type: String,
    val title: String,
    val description: String?,
    val durationSec: Int?,
    val level: String?, // BEGINNER, INTERMEDIATE, ADVANCED
    val goal: String?, // SLEEP, STRESS, ENERGY, FOCUS, RELAXATION, ANXIETY, MOOD
    val tagsJson: String, // JSON array of tags
    val contraindicationsJson: String, // JSON array of contraindications
    val patternId: String?,
    val audioUrl: String?,
    val usageCount: Int,
    val localesJson: String,
    val createdAt: Long?,
    val updatedAt: Long?,
    val stepsJson: String?,
    val safetyNotesJson: String?
)

@Entity(
    tableName = "practice_sessions",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PracticeEntity::class,
            parentColumns = ["id"],
            childColumns = ["practiceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DeviceEntity::class,
            parentColumns = ["id"],
            childColumns = ["deviceId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["practiceId"]),
        Index(value = ["deviceId"]),
        Index(value = ["status"]),
        Index(value = ["startedAt"], orders = [Index.Order.DESC])
    ]
)
data class PracticeSessionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val practiceId: String,
    val deviceId: String?,
    val status: String,
    val startedAt: Long,
    val completedAt: Long?,
    val durationSec: Int?,
    @ColumnInfo(defaultValue = "0") val completed: Boolean,
    val intensity: Double?,
    val brightness: Double?,
    val moodBefore: Int?,
    val moodAfter: Int?,
    val feedbackNote: String?,
    val source: String?
)
