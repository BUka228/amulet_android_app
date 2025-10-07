package com.example.amulet.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "remote_keys",
    indices = [Index(value = ["tableName", "partition"], unique = true)]
)
data class RemoteKeyEntity(
    @PrimaryKey val id: String,
    val tableName: String,
    val partition: RemoteKeyPartition,
    val nextCursor: String?,
    val prevCursor: String?,
    val updatedAt: Long
)

enum class RemoteKeyPartition(val value: String) {
    SENT("sent"),
    RECEIVED("received"),
    PUBLIC("public"),
    OWNED("owned"),
    MINE("mine"),
    DEFAULT("default");

    companion object {
        fun fromValue(value: String): RemoteKeyPartition =
            entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown remote key partition: $value")
    }
}
