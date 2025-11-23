package com.example.amulet.shared.domain.courses.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Условие разблокировки элемента курса.
 * Определяет, при каких условиях элемент становится доступным для пользователя.
 */
@Serializable
sealed class UnlockCondition {
    
    /**
     * Требуется завершение предыдущего элемента в курсе.
     */
    @Serializable
    @SerialName("complete_previous")
    data object CompletePreviousItem : UnlockCondition()
    
    /**
     * Требуется завершение конкретного элемента.
     * @param itemId ID элемента, который должен быть завершен
     */
    @Serializable
    @SerialName("complete_specific")
    data class CompleteSpecificItem(val itemId: CourseItemId) : UnlockCondition()
    
    /**
     * Требуется завершение N элементов из списка.
     * @param itemIds список ID элементов
     * @param count минимальное количество элементов, которые должны быть завершены
     */
    @Serializable
    @SerialName("complete_multiple")
    data class CompleteMultipleItems(
        val itemIds: List<CourseItemId>,
        val count: Int
    ) : UnlockCondition()
    
    /**
     * Требуется минимальный процент прогресса в курсе.
     * @param percent минимальный процент прогресса (0-100)
     */
    @Serializable
    @SerialName("minimum_progress")
    data class MinimumProgress(val percent: Int) : UnlockCondition()
    
    /**
     * Комбинация условий - все должны быть выполнены (логическое И).
     * @param conditions список условий, которые все должны быть выполнены
     */
    @Serializable
    @SerialName("and")
    data class And(val conditions: List<UnlockCondition>) : UnlockCondition()
    
    /**
     * Альтернативные условия - хотя бы одно должно быть выполнено (логическое ИЛИ).
     * @param conditions список условий, из которых хотя бы одно должно быть выполнено
     */
    @Serializable
    @SerialName("or")
    data class Or(val conditions: List<UnlockCondition>) : UnlockCondition()
}
