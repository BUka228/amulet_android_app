package com.example.amulet.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.amulet.core.database.entity.FirmwareInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FirmwareInfoDao {

    @Query("SELECT * FROM firmware_info WHERE hardwareVersion = :hardwareVersion ORDER BY cachedAt DESC")
    fun observeByHardwareVersion(hardwareVersion: Int): Flow<List<FirmwareInfoEntity>>

    @Query("SELECT * FROM firmware_info WHERE id = :id")
    fun observeById(id: String): Flow<FirmwareInfoEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(info: FirmwareInfoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(items: List<FirmwareInfoEntity>)

    @Query("DELETE FROM firmware_info WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM firmware_info WHERE cachedAt < :cutoff")
    suspend fun cleanup(cutoff: Long): Int

    @Query("DELETE FROM firmware_info")
    suspend fun clear()
}
