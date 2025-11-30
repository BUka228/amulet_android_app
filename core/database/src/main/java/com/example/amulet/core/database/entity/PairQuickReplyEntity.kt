package com.example.amulet.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "pair_quick_replies",
    primaryKeys = ["pairId", "userId", "gestureType"],
    foreignKeys = [
        ForeignKey(
            entity = PairEntity::class,
            parentColumns = ["id"],
            childColumns = ["pairId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PairEmotionEntity::class,
            parentColumns = ["id"],
            childColumns = ["emotionId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["pairId"]),
        Index(value = ["userId"]),
        Index(value = ["emotionId"])
    ]
)
data class PairQuickReplyEntity(
    val pairId: String,
    val userId: String,
    val gestureType: String,
    val emotionId: String?
)
