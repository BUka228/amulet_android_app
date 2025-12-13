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
        ),
        ForeignKey(
            entity = PlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["practiceId"]),
        Index(value = ["planId"])
    ]
)
data class PracticeScheduleEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val practiceId: String,
    val courseId: String? = null,
    val planId: String? = null,
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
    val preferredDurationsJson: String,
    val defaultAudioMode: String?,
    val hugsDndEnabled: Boolean?,
    val defaultHugColorHex: String?,
    val defaultHugPatternId: String?,
    val defaultHugEmotionId: String?,
)

@Entity(
    tableName = "plans",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class PlanEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String?,
    val status: String,
    val type: String?,
    val createdAt: Long,
    val updatedAt: Long?
)

@Entity(tableName = "user_practice_stats")
data class UserPracticeStatsEntity(
    @PrimaryKey val userId: String,
    val totalSessions: Int,
    val totalDurationSec: Int,
    val currentStreakDays: Int,
    val bestStreakDays: Int,
    val lastPracticeDate: Long?,
    val updatedAt: Long
)

@Entity(
    tableName = "user_badges",
    indices = [Index(value = ["userId"])]
)
data class UserBadgeEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val code: String,
    val earnedAt: Long,
    val metadataJson: String?
)

@Entity(
    tableName = "practice_tags",
    indices = [Index(value = ["name"], unique = true)]
)
data class PracticeTagEntity(
    @PrimaryKey val id: String,
    val name: String,
    val kind: String
)

@Entity(
    tableName = "practice_tag_cross_ref",
    primaryKeys = ["practiceId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = PracticeEntity::class,
            parentColumns = ["id"],
            childColumns = ["practiceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PracticeTagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["tagId"])]
)
data class PracticeTagCrossRef(
    val practiceId: String,
    val tagId: String
)

@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey val id: String,
    val code: String,
    val title: String,
    val description: String?,
    val order: Int?
)

@Entity(
    tableName = "collection_items",
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["collectionId"])]
)
data class CollectionItemEntity(
    @PrimaryKey val id: String,
    val collectionId: String,
    val type: String,
    val practiceId: String?,
    val courseId: String?,
    val order: Int
)

@Entity(
    tableName = "user_mood_entries",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId", "createdAt"])]
)
data class UserMoodEntryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val mood: String,
    val source: String,
    val createdAt: Long
)
