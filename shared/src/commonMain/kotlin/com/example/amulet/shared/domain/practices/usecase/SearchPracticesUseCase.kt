package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.practices.PracticesRepository
import com.example.amulet.shared.domain.practices.model.Practice
import com.example.amulet.shared.domain.practices.model.PracticeFilter

class SearchPracticesUseCase(
    private val repository: PracticesRepository
) {
    suspend operator fun invoke(
        query: String,
        filter: PracticeFilter
    ): AppResult<List<Practice>> = repository.search(query, filter)
}
