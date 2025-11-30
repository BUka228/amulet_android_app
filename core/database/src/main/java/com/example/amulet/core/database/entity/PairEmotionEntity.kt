package com.example.amulet.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pair_emotions",
    foreignKeys = [
        ForeignKey(
            entity = PairEntity::class,
            parentColumns = ["id"],
            childColumns = ["pairId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PatternEntity::class,
            parentColumns = ["id"],
            childColumns = ["patternId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["pairId"]),
        Index(value = ["patternId"]),
        Index(value = ["pairId", "order"], orders = [Index.Order.ASC, Index.Order.ASC])
    ]
)
data class PairEmotionEntity(
    @PrimaryKey val id: String,
    val pairId: String,
    val name: String,
    val colorHex: String,
    val patternId: String?,
    val order: Int
)
