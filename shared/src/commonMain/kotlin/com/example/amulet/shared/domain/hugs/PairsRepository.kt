package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.hugs.model.Pair
import com.example.amulet.shared.domain.hugs.model.PairEmotion
import com.example.amulet.shared.domain.hugs.model.PairId
import com.example.amulet.shared.domain.hugs.model.PairMemberSettings
import com.example.amulet.shared.domain.hugs.model.PairQuickReply
import com.example.amulet.shared.domain.user.model.UserId
import kotlinx.coroutines.flow.Flow

interface PairsRepository {

    fun observePairs(): Flow<List<Pair>>

    fun observePair(pairId: PairId): Flow<Pair?>

    fun observePairEmotions(pairId: PairId): Flow<List<PairEmotion>>

    suspend fun updatePairEmotions(
        pairId: PairId,
        emotions: List<PairEmotion>
    ): AppResult<Unit>

    suspend fun updateMemberSettings(
        pairId: PairId,
        userId: UserId,
        settings: PairMemberSettings
    ): AppResult<Unit>

    fun observeQuickReplies(
        pairId: PairId,
        userId: UserId
    ): Flow<List<PairQuickReply>>

    suspend fun updateQuickReplies(
        pairId: PairId,
        userId: UserId,
        replies: List<PairQuickReply>
    ): AppResult<Unit>
}
