package com.example.amulet.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "practice_categories",
    indices = [Index(value = ["order"])])
data class PracticeCategoryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val order: Int?
)

@Entity(
    tableName = "practice_favorites",
    primaryKeys = ["userId", "practiceId"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["practiceId"])])
data class PracticeFavoriteEntity(
    val userId: String,
    val practiceId: String,
    val createdAt: Long
)

@Entity(
    tableName = "practice_schedules",
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
        Index(value = ["practiceId"])
    ]
)
data class PracticeScheduleEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val practiceId: String,
    val courseId: String? = null,
    val daysOfWeekJson: String, // JSON array [1,2,3,4,5] for Mon-Fri
    val timeOfDay: String, // HH:mm format
    val reminderEnabled: Boolean,
    val createdAt: Long,
    val updatedAt: Long?
)

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val userId: String,
    val defaultIntensity: Double?,
    val defaultBrightness: Double?,
    val goalsJson: String,
    val interestsJson: String,
    val preferredDurationsJson: String
)
