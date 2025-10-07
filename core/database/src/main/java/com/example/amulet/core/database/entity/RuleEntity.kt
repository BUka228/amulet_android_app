package com.example.amulet.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rules",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["ownerId"]),
        Index(value = ["enabled"])
    ]
)
data class RuleEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val triggerJson: String,
    val actionJson: String,
    @ColumnInfo(defaultValue = "1") val enabled: Boolean,
    val scheduleJson: String?,
    val createdAt: Long?,
    val updatedAt: Long?
)
