package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.hugs.model.PairId
import com.example.amulet.shared.domain.hugs.model.PairQuickReply
import com.example.amulet.shared.domain.user.model.UserId

class UpdatePairQuickRepliesUseCase(
    private val repository: PairsRepository
) {
    suspend operator fun invoke(
        pairId: PairId,
        userId: UserId,
        replies: List<PairQuickReply>
    ): AppResult<Unit> = repository.updateQuickReplies(pairId, userId, replies)
}
