package com.example.amulet.core.database.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.amulet.core.database.entity.PairEntity
import com.example.amulet.core.database.entity.PairMemberEntity
import com.example.amulet.core.database.entity.UserEntity

data class PairWithMembers(
    @Embedded val pair: PairEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PairMemberEntity::class,
            parentColumn = "pairId",
            entityColumn = "userId"
        )
    )
    val members: List<UserEntity>
)
