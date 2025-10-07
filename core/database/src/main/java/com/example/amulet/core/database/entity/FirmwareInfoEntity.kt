package com.example.amulet.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "firmware_info",
    indices = [
        Index(value = ["hardwareVersion"]),
        Index(value = ["versionCode"])
    ]
)
data class FirmwareInfoEntity(
    @PrimaryKey val id: String,
    val hardwareVersion: Int,
    val versionName: String,
    val versionCode: Int,
    val downloadUrl: String,
    val changelog: String?,
    @ColumnInfo(defaultValue = "0") val mandatory: Boolean,
    val cachedAt: Long
)
