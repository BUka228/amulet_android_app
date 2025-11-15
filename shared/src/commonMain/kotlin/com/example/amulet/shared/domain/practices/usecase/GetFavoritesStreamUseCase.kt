package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.domain.practices.PracticesRepository
import com.example.amulet.shared.domain.practices.model.Practice
import kotlinx.coroutines.flow.Flow

class GetFavoritesStreamUseCase(
    private val repository: PracticesRepository
) {
    operator fun invoke(): Flow<List<Practice>> = repository.getFavoritesStream()
}
