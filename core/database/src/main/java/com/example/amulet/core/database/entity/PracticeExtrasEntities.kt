package com.example.amulet.core.database.entity

import androidx.room.Entity
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
    indices = [Index(value = ["practiceId"])])
data class PracticeFavoriteEntity(
    val userId: String,
    val practiceId: String
)

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val userId: String,
    val defaultIntensity: Double?,
    val defaultBrightness: Double?,
    val goalsJson: String,
    val interestsJson: String,
    val preferredDurationsJson: String
)
