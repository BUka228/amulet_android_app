package com.example.amulet.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.amulet.core.database.entity.RuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {

    @Query("SELECT * FROM rules WHERE ownerId = :ownerId ORDER BY updatedAt DESC")
    fun observeByOwner(ownerId: String): Flow<List<RuleEntity>>

    @Query("SELECT * FROM rules WHERE id = :ruleId")
    fun observeById(ruleId: String): Flow<RuleEntity?>

    @Query("SELECT * FROM rules WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    fun pagingByOwner(ownerId: String): PagingSource<Int, RuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(rule: RuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(rules: List<RuleEntity>)

    @Query("UPDATE rules SET enabled = :enabled, updatedAt = :updatedAt WHERE id = :ruleId")
    suspend fun updateEnabled(ruleId: String, enabled: Boolean, updatedAt: Long)

    @Query("DELETE FROM rules WHERE id = :ruleId")
    suspend fun delete(ruleId: String)

    @Query("DELETE FROM rules")
    suspend fun clear()
}
