package com.example.amulet.data.hugs.datasource.local

import com.example.amulet.core.database.entity.HugEntity
import com.example.amulet.core.database.entity.OutboxActionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Локальный источник данных для «объятий».
 * Инкапсулирует работу с HugDao и Room.
 */
interface HugsLocalDataSource {

    fun observeById(id: String): Flow<HugEntity?>

    fun observeByPairId(pairId: String): Flow<List<HugEntity>>

    fun observeByUserId(userId: String): Flow<List<HugEntity>>

    suspend fun upsert(entity: HugEntity)

    suspend fun upsert(entities: List<HugEntity>)

    suspend fun updateStatus(id: String, status: String)

    suspend fun enqueueOutboxAction(action: OutboxActionEntity)
}
