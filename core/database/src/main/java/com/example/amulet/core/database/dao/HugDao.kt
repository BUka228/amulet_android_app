package com.example.amulet.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.amulet.core.database.entity.HugEntity
import com.example.amulet.core.database.relation.HugWithParticipants
import kotlinx.coroutines.flow.Flow

@Dao
interface HugDao {

    @Query("SELECT * FROM hugs WHERE id = :id")
    fun observeById(id: String): Flow<HugEntity?>

    @Transaction
    @Query("SELECT * FROM hugs WHERE id = :id")
    fun observeWithParticipants(id: String): Flow<HugWithParticipants?>

    @Query("SELECT * FROM hugs ORDER BY createdAt DESC")
    fun pagingAll(): PagingSource<Int, HugEntity>

    @Query("SELECT * FROM hugs WHERE fromUserId = :userId ORDER BY createdAt DESC")
    fun pagingSent(userId: String): PagingSource<Int, HugEntity>

    @Query("SELECT * FROM hugs WHERE toUserId = :userId ORDER BY createdAt DESC")
    fun pagingReceived(userId: String): PagingSource<Int, HugEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: HugEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entities: List<HugEntity>)

    @Query("DELETE FROM hugs WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM hugs")
    suspend fun clear()
}
