package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.core.AppResult

class AcceptPairUseCase(
    private val repository: PairsRepository
) {
    suspend operator fun invoke(inviteId: String): AppResult<Unit> =
        repository.acceptPair(inviteId)
}
