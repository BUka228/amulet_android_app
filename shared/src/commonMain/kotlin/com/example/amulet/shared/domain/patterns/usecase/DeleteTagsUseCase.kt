package com.example.amulet.shared.domain.patterns.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.logging.Logger
import com.example.amulet.shared.domain.patterns.PatternsRepository

class DeleteTagsUseCase(
    private val repository: PatternsRepository
) {
    suspend operator fun invoke(names: List<String>): AppResult<Unit> {
        Logger.d("Удаление тегов из БД: ${names.size}", "DeleteTagsUseCase")
        return repository.deleteTags(names)
    }
}
