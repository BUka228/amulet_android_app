package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.domain.practices.PracticesRepository
import com.example.amulet.shared.domain.practices.model.Practice
import com.example.amulet.shared.domain.practices.model.PracticeId
import kotlinx.coroutines.flow.Flow

class GetPracticeByIdUseCase(
    private val repository: PracticesRepository
) {
    operator fun invoke(id: PracticeId): Flow<Practice?> = repository.getPracticeById(id)
}
