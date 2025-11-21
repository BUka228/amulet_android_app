package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.domain.practices.PracticesRepository
import com.example.amulet.shared.domain.practices.model.Practice
import kotlinx.coroutines.flow.Flow

class GetRecommendationsStreamUseCase(
    private val repository: PracticesRepository
) {
    operator fun invoke(limit: Int? = null, goal: com.example.amulet.shared.domain.practices.model.PracticeGoal? = null): Flow<List<Practice>> = 
        repository.getRecommendationsStream(limit, goal)
}
