package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.domain.practices.PracticesRepository
import com.example.amulet.shared.domain.practices.model.Practice
import com.example.amulet.shared.domain.practices.model.PracticeFilter
import kotlinx.coroutines.flow.Flow

class GetPracticesStreamUseCase(
    private val repository: PracticesRepository
) {
    operator fun invoke(
        filter: PracticeFilter
    ): Flow<List<Practice>> = repository.getPracticesStream(filter)
}
