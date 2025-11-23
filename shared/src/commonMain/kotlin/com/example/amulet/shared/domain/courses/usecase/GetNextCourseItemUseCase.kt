package com.example.amulet.shared.domain.courses.usecase

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.courses.CoursesRepository
import com.example.amulet.shared.domain.courses.model.CourseId
import com.example.amulet.shared.domain.courses.model.CourseItem
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kotlinx.coroutines.flow.first

/**
 * Use case для получения следующей незавершённой обязательной практики в курсе
 * с учётом unlock conditions
 */
class GetNextCourseItemUseCase(
    private val coursesRepository: CoursesRepository,
    private val checkItemUnlockUseCase: CheckItemUnlockUseCase
) {
    /**
     * @param courseId ID курса
     * @return Следующая незавершённая разблокированная обязательная практика,
     * или null если все практики завершены или заблокированы
     */
    suspend operator fun invoke(courseId: CourseId): AppResult<CourseItem?> {
        return try {
            val items = coursesRepository.getCourseItemsStream(courseId).first()
            val progress = coursesRepository.getCourseProgressStream(courseId).first()

            val completedIds = progress?.completedItemIds ?: emptySet()

            // Фильтруем только обязательные, незавершённые практики
            val candidates = items
                .filter { it.mandatory }
                .filter { it.id !in completedIds }
                .sortedBy { it.order }

            // Находим первую разблокированную
            val nextItem = candidates.firstOrNull { item ->
                val isUnlocked = checkItemUnlockUseCase(courseId, item.id).component1() ?: false
                isUnlocked
            }

            Ok(nextItem)
        } catch (e: Exception) {
            Err(AppError.Unknown)
        }
    }
}
