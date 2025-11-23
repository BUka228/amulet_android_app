package com.example.amulet.shared.domain.courses.usecase

import com.example.amulet.shared.domain.courses.CoursesRepository
import com.example.amulet.shared.domain.courses.model.CourseId
import com.example.amulet.shared.domain.courses.model.CourseItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Use case для получения списка разблокированных элементов курса.
 * 
 * @param coursesRepository репозиторий для получения данных о курсах
 * @param checkItemUnlockUseCase use case для проверки разблокировки элемента
 */
class GetUnlockedItemsUseCase(
    private val coursesRepository: CoursesRepository,
    private val checkItemUnlockUseCase: CheckItemUnlockUseCase
) {
    /**
     * Возвращает Flow со списком разблокированных элементов курса.
     * 
     * @param courseId ID курса
     * @return Flow со списком доступных элементов
     */
    operator fun invoke(courseId: CourseId): Flow<List<CourseItem>> {
        return combine(
            coursesRepository.getCourseItemsStream(courseId),
            coursesRepository.getCourseProgressStream(courseId)
        ) { items, _ ->
            // Фильтруем элементы, оставляя только разблокированные
            items.filter { item ->
                val isUnlocked = checkItemUnlockUseCase(courseId, item.id).component1() ?: false
                isUnlocked
            }
        }
    }
}
