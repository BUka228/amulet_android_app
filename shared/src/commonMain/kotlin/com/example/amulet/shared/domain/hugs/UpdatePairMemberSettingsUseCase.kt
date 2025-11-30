package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.hugs.model.PairId
import com.example.amulet.shared.domain.hugs.model.PairMemberSettings
import com.example.amulet.shared.domain.user.model.UserId

class UpdatePairMemberSettingsUseCase(
    private val repository: PairsRepository
) {
    suspend operator fun invoke(
        pairId: PairId,
        userId: UserId,
        settings: PairMemberSettings
    ): AppResult<Unit> = repository.updateMemberSettings(pairId, userId, settings)
}
