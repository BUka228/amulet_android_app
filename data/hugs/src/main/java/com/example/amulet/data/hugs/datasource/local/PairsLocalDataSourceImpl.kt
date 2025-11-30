package com.example.amulet.data.hugs.datasource.local

import com.example.amulet.core.database.dao.PairDao
import com.example.amulet.core.database.entity.PairEmotionEntity
import com.example.amulet.core.database.entity.PairQuickReplyEntity
import com.example.amulet.core.database.relation.PairWithMemberSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PairsLocalDataSourceImpl @Inject constructor(
    private val pairDao: PairDao
) : PairsLocalDataSource {

    override fun observeAllWithSettings(): Flow<List<PairWithMemberSettings>> =
        pairDao.observeAllWithSettings()

    override fun observePairWithSettings(pairId: String): Flow<PairWithMemberSettings?> =
        pairDao.observePairWithSettings(pairId)

    override fun observeEmotions(pairId: String): Flow<List<PairEmotionEntity>> =
        pairDao.observeEmotions(pairId)

    override suspend fun upsertEmotions(entities: List<PairEmotionEntity>) {
        pairDao.upsertEmotions(entities)
    }

    override suspend fun updateMemberSettings(
        pairId: String,
        userId: String,
        muted: Boolean,
        quietStart: Int?,
        quietEnd: Int?,
        maxHugsPerHour: Int?
    ) {
        pairDao.updateMemberSettings(pairId, userId, muted, quietStart, quietEnd, maxHugsPerHour)
    }

    override fun observeQuickReplies(pairId: String, userId: String): Flow<List<PairQuickReplyEntity>> =
        pairDao.observeQuickReplies(pairId, userId)

    override suspend fun upsertQuickReplies(entities: List<PairQuickReplyEntity>) {
        pairDao.upsertQuickReplies(entities)
    }
}
