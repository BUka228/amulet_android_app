package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.hugs.model.PairId
import com.example.amulet.shared.domain.hugs.model.PairQuickReply
import com.example.amulet.shared.domain.user.model.UserId

interface SendHugUseCase {

    suspend operator fun invoke(
        pairId: PairId?,
        fromUserId: UserId,
        toUserId: UserId?,
        quickReply: PairQuickReply? = null,
        payload: Map<String, Any?>? = null
    ): AppResult<Unit>
}
