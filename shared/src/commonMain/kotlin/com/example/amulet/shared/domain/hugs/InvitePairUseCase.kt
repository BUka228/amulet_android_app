package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.logging.Logger
import com.example.amulet.shared.domain.hugs.model.PairInvite

class InvitePairUseCase(
    private val repository: PairsRepository
) {
    suspend operator fun invoke(
        method: String = "link",
        target: String? = null,
    ): AppResult<PairInvite> {
        Logger.d("InvitePairUseCase: invoke(method=$method, target=$target)", "InvitePairUseCase")
        val result = repository.invitePair(method, target)
        result.component1()?.let { invite ->
            Logger.d(
                "InvitePairUseCase: success inviteId=${invite.inviteId} url=${invite.url}",
                "InvitePairUseCase"
            )
        }
        result.component2()?.let { error ->
            Logger.e(
                "InvitePairUseCase: failure error=$error",
                throwable = Exception(error.toString()),
                tag = "InvitePairUseCase"
            )
        }
        return result
    }
}
