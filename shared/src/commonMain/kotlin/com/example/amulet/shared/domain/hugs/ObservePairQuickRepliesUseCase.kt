package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.domain.hugs.model.PairId
import com.example.amulet.shared.domain.hugs.model.PairQuickReply
import com.example.amulet.shared.domain.user.model.UserId
import kotlinx.coroutines.flow.Flow

class ObservePairQuickRepliesUseCase(
    private val repository: PairsRepository
) {
    operator fun invoke(pairId: PairId, userId: UserId): Flow<List<PairQuickReply>> =
        repository.observeQuickReplies(pairId, userId)
}
