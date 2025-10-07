package com.example.amulet.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "telemetry_events",
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
        Index(value = ["timestamp"], orders = [Index.Order.DESC]),
        Index(value = ["sentAt"])
    ]
)
data class TelemetryEventEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val type: String,
    val payloadJson: String,
    val createdAt: Long,
    val timestamp: Long,
    val sentAt: Long?
)
