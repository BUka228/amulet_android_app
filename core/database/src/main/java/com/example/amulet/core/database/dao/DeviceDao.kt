package com.example.amulet.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.amulet.core.database.entity.DeviceEntity
import com.example.amulet.core.database.relation.DeviceWithOwner
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {

    @Query("SELECT * FROM devices WHERE ownerId = :ownerId ORDER BY updatedAt DESC")
    fun observeByOwner(ownerId: String): Flow<List<DeviceEntity>>

    @Transaction
    @Query("SELECT * FROM devices WHERE id = :deviceId")
    fun observeWithOwner(deviceId: String): Flow<DeviceWithOwner?>

    @Query("SELECT * FROM devices WHERE id = :deviceId LIMIT 1")
    suspend fun getById(deviceId: String): DeviceEntity?

    @Query("SELECT * FROM devices ORDER BY updatedAt DESC")
    fun pagingAll(): PagingSource<Int, DeviceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(device: DeviceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(devices: List<DeviceEntity>)

    @Query("DELETE FROM devices WHERE id = :deviceId")
    suspend fun deleteById(deviceId: String)

    @Query("DELETE FROM devices")
    suspend fun clear()
}
