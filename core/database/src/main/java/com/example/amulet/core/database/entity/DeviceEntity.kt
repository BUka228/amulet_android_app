package com.example.amulet.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "devices",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["ownerId"])
    ]
)
data class DeviceEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val bleAddress: String,
    val hardwareVersion: Int,
    val firmwareVersion: String?,
    val name: String?,
    val batteryLevel: Double?,
    val status: DeviceStatus?,
    val settingsJson: String,
    val addedAt: Long,
    val lastConnectedAt: Long?
)

enum class DeviceStatus(val value: String) {
    ONLINE("online"),
    OFFLINE("offline"),
    CHARGING("charging"),
    ERROR("error"),
    BANNED("banned");

    companion object {
        fun fromValue(value: String): DeviceStatus =
            entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown device status: $value")
    }
}
