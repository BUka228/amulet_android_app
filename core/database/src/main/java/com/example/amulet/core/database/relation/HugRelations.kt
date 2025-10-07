package com.example.amulet.core.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.amulet.core.database.entity.HugEntity
import com.example.amulet.core.database.entity.PatternEntity
import com.example.amulet.core.database.entity.UserEntity

data class HugWithParticipants(
    @Embedded val hug: HugEntity,
    @Relation(
        parentColumn = "fromUserId",
        entityColumn = "id"
    )
    val fromUser: UserEntity?,
    @Relation(
        parentColumn = "toUserId",
        entityColumn = "id"
    )
    val toUser: UserEntity?,
    @Relation(
        parentColumn = "emotionPatternId",
        entityColumn = "id"
    )
    val pattern: PatternEntity?
)
