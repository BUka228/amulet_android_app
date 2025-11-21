package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.practices.PracticesRepository
import com.example.amulet.shared.domain.practices.model.PracticeSchedule

class UpsertPracticeScheduleUseCase(
    private val repository: PracticesRepository
) {
    suspend operator fun invoke(schedule: PracticeSchedule): AppResult<Unit> =
        repository.upsertSchedule(schedule)
}
