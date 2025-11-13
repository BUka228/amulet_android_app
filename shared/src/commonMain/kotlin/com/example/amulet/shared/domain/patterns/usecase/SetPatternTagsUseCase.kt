package com.example.amulet.shared.domain.patterns.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.logging.Logger
import com.example.amulet.shared.domain.patterns.PatternsRepository
import com.example.amulet.shared.domain.patterns.model.PatternId

class SetPatternTagsUseCase(
    private val repository: PatternsRepository
) {
    suspend operator fun invoke(
        patternId: PatternId,
        tags: List<String>
    ): AppResult<Unit> {
        Logger.d("Установка тегов паттерна: ${patternId.value} -> ${tags.size}", "SetPatternTagsUseCase")
        return repository.setPatternTags(patternId, tags)
    }
}
