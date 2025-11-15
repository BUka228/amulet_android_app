package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.domain.practices.PracticesRepository
import com.example.amulet.shared.domain.practices.model.PracticeSession
import kotlinx.coroutines.flow.Flow

class GetActiveSessionStreamUseCase(
    private val repository: PracticesRepository
) {
    operator fun invoke(): Flow<PracticeSession?> = repository.getActiveSessionStream()
}
