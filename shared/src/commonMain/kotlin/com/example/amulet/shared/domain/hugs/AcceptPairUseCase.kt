package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.logging.Logger

class AcceptPairUseCase(
    private val repository: PairsRepository
) {
    suspend operator fun invoke(inviteId: String): AppResult<Unit> {
        Logger.d("AcceptPairUseCase: invoke(inviteId=$inviteId)", "AcceptPairUseCase")
        val result = repository.acceptPair(inviteId)
        result.component1()?.let {
            Logger.d("AcceptPairUseCase: success inviteId=$inviteId", "AcceptPairUseCase")
        }
        result.component2()?.let { error ->
            Logger.e(
                "AcceptPairUseCase: failure error=$error",
                throwable = Exception(error.toString()),
                tag = "AcceptPairUseCase"
            )
        }
        return result
    }
}
