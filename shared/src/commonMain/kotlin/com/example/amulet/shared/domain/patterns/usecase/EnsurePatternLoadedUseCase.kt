package com.example.amulet.shared.domain.patterns.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.patterns.PatternsRepository
import com.example.amulet.shared.domain.patterns.model.PatternId

/**
 * UseCase, который гарантирует наличие паттерна локально.
 * Если паттерна нет в БД, репозиторий попытается загрузить его с сервера и сохранить.
 */
class EnsurePatternLoadedUseCase(
    private val repository: PatternsRepository,
) {
    suspend operator fun invoke(id: PatternId): AppResult<Unit> =
        repository.ensurePatternLoaded(id)
}
