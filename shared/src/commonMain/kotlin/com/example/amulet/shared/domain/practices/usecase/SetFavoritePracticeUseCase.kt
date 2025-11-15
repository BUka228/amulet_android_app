package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.practices.PracticesRepository
import com.example.amulet.shared.domain.practices.model.PracticeId

class SetFavoritePracticeUseCase(
    private val repository: PracticesRepository
) {
    suspend operator fun invoke(
        practiceId: PracticeId,
        favorite: Boolean
    ): AppResult<Unit> = repository.setFavorite(practiceId, favorite)
}
