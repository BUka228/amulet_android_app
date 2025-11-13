package com.example.amulet.shared.domain.patterns.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.logging.Logger
import com.example.amulet.shared.domain.patterns.PatternsRepository

class GetAllTagsUseCase(
    private val repository: PatternsRepository
) {
    suspend operator fun invoke(): AppResult<List<String>> {
        Logger.d("Загрузка всех тегов", "GetAllTagsUseCase")
        return repository.getAllTags()
    }
}
