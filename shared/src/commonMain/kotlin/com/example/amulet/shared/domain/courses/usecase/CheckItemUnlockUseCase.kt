package com.example.amulet.shared.domain.courses.usecase

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.courses.CoursesRepository
import com.example.amulet.shared.domain.courses.model.CourseId
import com.example.amulet.shared.domain.courses.model.CourseItemId
import com.example.amulet.shared.domain.courses.model.UnlockCondition
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kotlinx.coroutines.flow.firstOrNull

/**
 * Use case для проверки условий разблокировки элемента курса.
 * 
 * @param coursesRepository репозиторий для получения данных о курсах и прогрессе
 */
class CheckItemUnlockUseCase(
    private val coursesRepository: CoursesRepository
) {
    /**
     * Проверяет, разблокирован ли элемент курса.
     * 
     * @param courseId ID курса
     * @param itemId ID элемента курса
     * @return true, если элемент разблокирован, false в противном случае
     */
    suspend operator fun invoke(courseId: CourseId, itemId: CourseItemId): AppResult<Boolean> {
        return try {
            val items = coursesRepository.getCourseItemsStream(courseId).firstOrNull() ?: return Ok(false)
            val item = items.find { it.id == itemId } ?: return Ok(false)

            // Если условие разблокировки не задано, элемент доступен по умолчанию
            val unlockCondition = item.unlockCondition ?: return Ok(true)

            val progress = coursesRepository.getCourseProgressStream(courseId).firstOrNull()
            val completedItemIds = progress?.completedItemIds ?: emptySet()

            Ok(
                checkCondition(
                    condition = unlockCondition,
                    currentItem = item,
                    allItems = items,
                    completedItemIds = completedItemIds,
                    currentPercent = progress?.percent ?: 0
                )
            )
        } catch (e: Exception) {
            Err(AppError.Unknown)
        }
    }
    
    /**
     * Рекурсивно проверяет условие разблокировки.
     */
    private fun checkCondition(
        condition: UnlockCondition,
        currentItem: com.example.amulet.shared.domain.courses.model.CourseItem,
        allItems: List<com.example.amulet.shared.domain.courses.model.CourseItem>,
        completedItemIds: Set<String>,
        currentPercent: Int
    ): Boolean {
        return when (condition) {
            is UnlockCondition.CompletePreviousItem -> {
                // Найти предыдущий элемент по порядку
                val previousItem = allItems
                    .filter { it.order < currentItem.order }
                    .maxByOrNull { it.order }
                
                previousItem?.let { completedItemIds.contains(it.id) } ?: true
            }
            
            is UnlockCondition.CompleteSpecificItem -> {
                completedItemIds.contains(condition.itemId)
            }
            
            is UnlockCondition.CompleteMultipleItems -> {
                val completedCount = condition.itemIds.count { completedItemIds.contains(it) }
                completedCount >= condition.count
            }
            
            is UnlockCondition.MinimumProgress -> {
                currentPercent >= condition.percent
            }
            
            is UnlockCondition.And -> {
                condition.conditions.all { 
                    checkCondition(it, currentItem, allItems, completedItemIds, currentPercent) 
                }
            }
            
            is UnlockCondition.Or -> {
                condition.conditions.any { 
                    checkCondition(it, currentItem, allItems, completedItemIds, currentPercent) 
                }
            }
        }
    }
}
