package com.example.amulet.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.amulet.core.database.entity.RemoteKeyEntity
import com.example.amulet.core.database.entity.RemoteKeyPartition

@Dao
interface RemoteKeyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(key: RemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(keys: List<RemoteKeyEntity>)

    @Query("SELECT * FROM remote_keys WHERE tableName = :tableName AND partition = :partition LIMIT 1")
    suspend fun get(tableName: String, partition: RemoteKeyPartition): RemoteKeyEntity?

    @Query("DELETE FROM remote_keys WHERE tableName = :tableName AND partition = :partition")
    suspend fun delete(tableName: String, partition: RemoteKeyPartition)

    @Query("DELETE FROM remote_keys WHERE updatedAt < :cutoff")
    suspend fun cleanupOlderThan(cutoff: Long): Int

    @Query("DELETE FROM remote_keys")
    suspend fun clear()
}
