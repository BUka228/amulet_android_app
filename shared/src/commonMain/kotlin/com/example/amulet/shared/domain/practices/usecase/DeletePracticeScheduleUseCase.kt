package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.practices.PracticesRepository

class DeletePracticeScheduleUseCase(
    private val repository: PracticesRepository
) {
    suspend operator fun invoke(scheduleId: String): AppResult<Unit> = repository.deleteSchedule(scheduleId)
}
