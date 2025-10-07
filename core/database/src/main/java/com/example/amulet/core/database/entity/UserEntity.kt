package com.example.amulet.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["updatedAt"])]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val displayName: String?,
    val avatarUrl: String?,
    val timezone: String?,
    val language: String?,
    val consentsJson: String,
    val createdAt: Long?,
    val updatedAt: Long?
)
