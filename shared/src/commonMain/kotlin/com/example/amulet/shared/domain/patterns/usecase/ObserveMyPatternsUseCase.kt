package com.example.amulet.shared.domain.patterns.usecase

import com.example.amulet.shared.domain.patterns.PatternsRepository
import com.example.amulet.shared.domain.patterns.model.Pattern
import kotlinx.coroutines.flow.Flow

/**
 * UseCase для наблюдения за паттернами текущего пользователя.
 */
class ObserveMyPatternsUseCase(
    private val repository: PatternsRepository
) {
    operator fun invoke(): Flow<List<Pattern>> {
        return repository.getMyPatternsStream()
    }
}
