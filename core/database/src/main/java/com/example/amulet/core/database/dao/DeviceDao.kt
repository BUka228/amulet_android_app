package com.example.amulet.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.amulet.core.database.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {

    @Query("SELECT * FROM devices WHERE ownerId = :ownerId ORDER BY addedAt DESC")
    fun observeByOwner(ownerId: String): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE id = :deviceId LIMIT 1")
    suspend fun getById(deviceId: String): DeviceEntity?

    @Query("SELECT * FROM devices WHERE bleAddress = :bleAddress AND ownerId = :ownerId LIMIT 1")
    suspend fun getByBleAddress(bleAddress: String, ownerId: String): DeviceEntity?

    @Query("SELECT * FROM devices WHERE ownerId = :ownerId AND lastConnectedAt IS NOT NULL ORDER BY lastConnectedAt DESC LIMIT 1")
    suspend fun getLastConnectedForOwner(ownerId: String): DeviceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(device: DeviceEntity)

    @Query("DELETE FROM devices WHERE id = :deviceId")
    suspend fun deleteById(deviceId: String)

    @Query("DELETE FROM devices WHERE ownerId = :ownerId")
    suspend fun clearByOwner(ownerId: String)
    
    @Query("DELETE FROM devices")
    suspend fun clear()
}
