package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.domain.practices.PracticesRepository
import com.example.amulet.shared.domain.practices.model.PracticeSession
import kotlinx.coroutines.flow.Flow

class GetSessionsHistoryStreamUseCase(
    private val repository: PracticesRepository
) {
    operator fun invoke(limit: Int? = null): Flow<List<PracticeSession>> = repository.getSessionsHistoryStream(limit)
}
