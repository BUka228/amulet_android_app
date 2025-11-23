package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.practices.PracticesRepository
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kotlinx.coroutines.flow.first

/**
 * Удаляет все расписания практик, привязанные к конкретному курсу
 */
class DeleteSchedulesForCourseUseCase(
    private val repository: PracticesRepository
) {
    /**
     * @return количество удалённых расписаний
     */
    suspend operator fun invoke(courseId: String): AppResult<Int> = try {
        val schedules = repository.getSchedulesStream().first()
        val toDelete = schedules.filter { it.courseId == courseId }
        toDelete.forEach { schedule ->
            repository.deleteSchedule(schedule.id)
        }
        Ok(toDelete.size)
    } catch (e: Exception) {
        Err(AppError.Unknown)
    }
}
