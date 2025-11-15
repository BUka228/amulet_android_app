package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.domain.practices.PracticesRepository
import com.example.amulet.shared.domain.practices.model.PracticeCategory
import kotlinx.coroutines.flow.Flow

class GetCategoriesStreamUseCase(
    private val repository: PracticesRepository
) {
    operator fun invoke(): Flow<List<PracticeCategory>> = repository.getCategoriesStream()
}
