package com.example.amulet.data.hugs.datasource.local

import com.example.amulet.core.database.entity.OutboxActionEntity
import com.example.amulet.core.database.entity.PairEmotionEntity
import com.example.amulet.core.database.entity.PairQuickReplyEntity
import com.example.amulet.core.database.relation.PairWithMemberSettings
import kotlinx.coroutines.flow.Flow

/**
 * Локальный источник данных для пар и настроек «объятий».
 */
interface PairsLocalDataSource {

    fun observeAllWithSettings(): Flow<List<PairWithMemberSettings>>

    fun observePairWithSettings(pairId: String): Flow<PairWithMemberSettings?>

    fun observeEmotions(pairId: String): Flow<List<PairEmotionEntity>>

    suspend fun upsertEmotions(entities: List<PairEmotionEntity>)

    suspend fun updateMemberSettings(
        pairId: String,
        userId: String,
        muted: Boolean,
        quietStart: Int?,
        quietEnd: Int?,
        maxHugsPerHour: Int?
    )

    fun observeQuickReplies(pairId: String, userId: String): Flow<List<PairQuickReplyEntity>>

    suspend fun upsertQuickReplies(entities: List<PairQuickReplyEntity>)

    suspend fun enqueueOutboxAction(action: OutboxActionEntity)

    suspend fun <R> withPairTransaction(block: suspend () -> R): R
}
