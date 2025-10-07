package com.example.amulet.core.database.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.amulet.core.database.entity.PatternEntity
import com.example.amulet.core.database.entity.PatternShareEntity
import com.example.amulet.core.database.entity.PatternTagCrossRef
import com.example.amulet.core.database.entity.TagEntity
import com.example.amulet.core.database.entity.UserEntity

data class PatternWithRelations(
    @Embedded val pattern: PatternEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PatternTagCrossRef::class,
            parentColumn = "patternId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PatternShareEntity::class,
            parentColumn = "patternId",
            entityColumn = "userId"
        )
    )
    val sharedWith: List<UserEntity>
)
