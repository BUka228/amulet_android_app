package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.domain.practices.PracticesRepository
import com.example.amulet.shared.domain.practices.model.PracticeId
import com.example.amulet.shared.domain.practices.model.PracticeSchedule
import kotlinx.coroutines.flow.Flow

class GetScheduleByPracticeIdUseCase(
    private val repository: PracticesRepository
) {
    operator fun invoke(practiceId: PracticeId): Flow<PracticeSchedule?> =
        repository.getScheduleByPracticeId(practiceId)
}
