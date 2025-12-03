package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.hugs.model.PairInvite

class InvitePairUseCase(
    private val repository: PairsRepository
) {
    suspend operator fun invoke(
        method: String = "link",
        target: String? = null,
    ): AppResult<PairInvite> = repository.invitePair(method, target)
}
