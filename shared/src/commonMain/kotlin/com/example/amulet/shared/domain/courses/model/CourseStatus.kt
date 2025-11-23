package com.example.amulet.shared.domain.courses.model

/**
 * Состояние курса для пользователя
 */
enum class CourseStatus {
    /**
     * Курс не начат, прогресс = 0, нет плана
     */
    NOT_ENROLLED,
    
    /**
     * Курс в процессе, есть прогресс (>0), есть или нет план
     */
    IN_PROGRESS,
    
    /**
     * Все обязательные практики пройдены, курс можно проходить повторно
     */
    COMPLETED
}
