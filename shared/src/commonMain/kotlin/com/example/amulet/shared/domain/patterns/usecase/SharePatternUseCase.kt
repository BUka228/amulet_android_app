package com.example.amulet.shared.domain.patterns.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.logging.Logger
import com.example.amulet.shared.domain.patterns.PatternsRepository
import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.user.model.UserId

/**
 * UseCase для шаринга паттерна с другими пользователями.
 */
class SharePatternUseCase(
    private val repository: PatternsRepository
) {
    suspend operator fun invoke(
        id: PatternId,
        userIds: List<UserId>
    ): AppResult<Unit> {
        Logger.d("Шаринг паттерна: $id, пользователям: ${userIds.size}", "SharePatternUseCase")
        return repository.sharePattern(id, userIds)
    }
}
