package com.example.amulet.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "courses",
    indices = [
        Index(value = ["title"]),
        Index(value = ["goal"]),
        Index(value = ["level"])
    ])
data class CourseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val goal: String?, // Same as PracticeGoal
    val level: String?, // Same as PracticeLevel
    val rhythm: String, // DAILY, THREE_TIMES_WEEK, FLEXIBLE
    val tagsJson: String,
    val totalDurationSec: Int?,
    val modulesCount: Int,
    val recommendedDays: Int?,
    val difficulty: String?,
    val coverUrl: String?,
    val createdAt: Long?,
    val updatedAt: Long?
)

@Entity(
    tableName = "course_items",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["courseId"])])
data class CourseItemEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val order: Int,
    val type: String,
    val practiceId: String?,
    val title: String?,
    val description: String?,
    val mandatory: Boolean,
    val minDurationSec: Int?
)

@Entity(
    tableName = "course_progress",
    primaryKeys = ["userId", "courseId"],
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["courseId"])])
data class CourseProgressEntity(
    val userId: String,
    val courseId: String,
    val completedItemIdsJson: String,
    val currentItemId: String?,
    val percent: Int,
    val totalTimeSec: Int,
    val updatedAt: Long?
)
