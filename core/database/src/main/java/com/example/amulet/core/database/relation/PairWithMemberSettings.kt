package com.example.amulet.core.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.amulet.core.database.entity.PairEntity
import com.example.amulet.core.database.entity.PairMemberEntity

data class PairWithMemberSettings(
    @Embedded val pair: PairEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "pairId"
    )
    val members: List<PairMemberEntity>
)
