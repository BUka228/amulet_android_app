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
    fun observeById(id: String): Flow<PatternEntity?>
    
    @Transaction
    @Query("SELECT * FROM patterns WHERE id = :id")
    fun observeByIdWithRelations(id: String): Flow<PatternWithRelations?>

    @Transaction
    @Query("SELECT * FROM patterns WHERE ownerId = :ownerId ORDER BY updatedAt DESC, createdAt DESC")
    fun observeByOwner(ownerId: String): Flow<List<PatternEntity>>

    @Transaction
    @Query("SELECT * FROM patterns WHERE public = 1")
    fun observePublic(): Flow<List<PatternEntity>>
    
    @Query("SELECT * FROM patterns WHERE ownerId IS NULL")
    fun observePresets(): Flow<List<PatternEntity>>
    
    @Transaction
    @Query("SELECT * FROM patterns WHERE public = 1 ORDER BY createdAt DESC")
    fun pagingPublic(): PagingSource<Int, PatternWithRelations>
    
    @Transaction
    @Query("""
        SELECT p.* FROM patterns p
        INNER JOIN pattern_shares ps ON p.id = ps.patternId
        WHERE ps.userId = :userId
        ORDER BY p.updatedAt DESC, p.createdAt DESC
    """)
    fun observeSharedWith(userId: String): Flow<List<PatternEntity>>
    
    @Query("""
        SELECT t.* FROM tags t
        INNER JOIN pattern_tags pt ON t.id = pt.tagId
        WHERE pt.patternId = :patternId
    """)
    suspend fun getTagsForPattern(patternId: String): List<TagEntity>
    
    @Query("SELECT * FROM pattern_shares WHERE patternId = :patternId")
    suspend fun getSharesForPattern(patternId: String): List<PatternShareEntity>

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

    @Query("SELECT * FROM tags ORDER BY name ASC")
    suspend fun getAllTags(): List<TagEntity>

    @Query("SELECT * FROM tags WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    suspend fun searchTags(query: String): List<TagEntity>

    @Query("SELECT * FROM tags WHERE name IN (:names)")
    suspend fun getTagsByNames(names: List<String>): List<TagEntity>

    @Query("DELETE FROM tags WHERE name IN (:names)")
    suspend fun deleteTagsByNames(names: List<String>)
}

