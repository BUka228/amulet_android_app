package com.example.amulet.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pairs",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["blockedBy"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["status"]),
        Index(value = ["createdAt"], orders = [Index.Order.DESC]),
        Index(value = ["blockedBy"])
    ]
)
data class PairEntity(
    @PrimaryKey val id: String,
    val status: String,
    val blockedBy: String?,
    val blockedAt: Long?,
    val createdAt: Long
)

@Entity(
    tableName = "pair_members",
    primaryKeys = ["pairId", "userId"],
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
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class PairMemberEntity(
    val pairId: String,
    val userId: String,
    val joinedAt: Long,
    // User-level settings in pair
    val muted: Boolean = false,
    val quietHoursStartMinutes: Int? = null,
    val quietHoursEndMinutes: Int? = null,
    val maxHugsPerHour: Int? = null
)
