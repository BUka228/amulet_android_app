package com.example.amulet.core.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.amulet.core.database.entity.DeviceEntity
import com.example.amulet.core.database.entity.PracticeEntity
import com.example.amulet.core.database.entity.PracticeSessionEntity
import com.example.amulet.core.database.entity.UserEntity

data class PracticeSessionWithDetails(
    @Embedded val session: PracticeSessionEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "id"
    )
    val user: UserEntity,
    @Relation(
        parentColumn = "practiceId",
        entityColumn = "id"
    )
    val practice: PracticeEntity,
    @Relation(
        parentColumn = "deviceId",
        entityColumn = "id"
    )
    val device: DeviceEntity?
)
