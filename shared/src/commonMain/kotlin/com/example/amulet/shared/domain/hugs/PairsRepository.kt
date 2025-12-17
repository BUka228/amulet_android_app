package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.hugs.model.Pair
import com.example.amulet.shared.domain.hugs.model.PairEmotion
import com.example.amulet.shared.domain.hugs.model.PairId
import com.example.amulet.shared.domain.hugs.model.PairInvite
import com.example.amulet.shared.domain.hugs.model.PairMemberSettings
import com.example.amulet.shared.domain.hugs.model.PairQuickReply
import com.example.amulet.shared.domain.user.model.UserId
import kotlinx.coroutines.flow.Flow

interface PairsRepository {

    suspend fun invitePair(method: String, target: String? = null): AppResult<PairInvite>

    suspend fun acceptPair(inviteId: String): AppResult<Unit>

    suspend fun syncPairs(): AppResult<Unit>

    suspend fun fetchPairEmotionsFromRemote(pairId: PairId): AppResult<List<PairEmotion>>

    suspend fun upsertPairEmotionsLocal(pairId: PairId, emotions: List<PairEmotion>): AppResult<Unit>

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

    suspend fun blockPair(pairId: PairId): AppResult<Unit>

    suspend fun unblockPair(pairId: PairId): AppResult<Unit>

    suspend fun deletePair(pairId: PairId): AppResult<Unit>

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
