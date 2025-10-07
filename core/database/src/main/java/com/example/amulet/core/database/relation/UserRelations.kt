package com.example.amulet.core.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.amulet.core.database.entity.DeviceEntity
import com.example.amulet.core.database.entity.UserEntity

data class UserWithDevices(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "ownerId"
    )
    val devices: List<DeviceEntity>
)

data class DeviceWithOwner(
    @Embedded val device: DeviceEntity,
    @Relation(
        parentColumn = "ownerId",
        entityColumn = "id"
    )
    val owner: UserEntity?
)
