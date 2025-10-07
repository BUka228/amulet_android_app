package com.example.amulet.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.amulet.core.database.entity.PatternEntity
import com.example.amulet.core.database.entity.PatternShareEntity
import com.example.amulet.core.database.entity.PatternTagCrossRef
import com.example.amulet.core.database.entity.TagEntity
import com.example.amulet.core.database.relation.PatternWithRelations
import kotlinx.coroutines.flow.Flow

@Dao
interface PatternDao {

    @Transaction
    @Query("SELECT * FROM patterns WHERE id = :id")
    fun observeById(id: String): Flow<PatternWithRelations?>

    @Transaction
    @Query("SELECT * FROM patterns WHERE ownerId = :ownerId ORDER BY updatedAt DESC, createdAt DESC")
    fun pagingOwned(ownerId: String): PagingSource<Int, PatternWithRelations>

    @Transaction
    @Query("SELECT * FROM patterns WHERE public = 1 ORDER BY createdAt DESC")
    fun pagingPublic(): PagingSource<Int, PatternWithRelations>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPattern(pattern: PatternEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPatterns(patterns: List<PatternEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTags(tags: List<TagEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatternTags(relations: List<PatternTagCrossRef>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatternShares(relations: List<PatternShareEntity>)

    @Query("DELETE FROM pattern_tags WHERE patternId = :patternId")
    suspend fun deletePatternTags(patternId: String)

    @Query("DELETE FROM pattern_shares WHERE patternId = :patternId")
    suspend fun deletePatternShares(patternId: String)

    @Query("DELETE FROM patterns WHERE id = :patternId")
    suspend fun deletePattern(patternId: String)

    @Query("DELETE FROM patterns")
    suspend fun clear()

    @Transaction
    suspend fun upsertPatternWithRelations(
        pattern: PatternEntity,
        tags: List<TagEntity>,
        tagIds: List<String>,
        sharedUserIds: List<String>
    ) {
        upsertPattern(pattern)
        if (tags.isNotEmpty()) insertTags(tags)
        deletePatternTags(pattern.id)
        if (tagIds.isNotEmpty()) {
            insertPatternTags(tagIds.map { tagId -> PatternTagCrossRef(pattern.id, tagId) })
        }
        deletePatternShares(pattern.id)
        if (sharedUserIds.isNotEmpty()) {
            insertPatternShares(sharedUserIds.map { userId -> PatternShareEntity(pattern.id, userId) })
        }
    }
}
