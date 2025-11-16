package com.example.amulet.shared.domain.initialization

import com.example.amulet.shared.core.AppResult

/**
 * Интерфейс для инициализации данных приложения.
 * Реализация находится в app модуле с использованием UseCase.
 */
interface DataInitializer {
    /**
     * Инициализирует данные приложения при первом запуске.
     * @return AppResult<Unit> - результат операции
     */
    suspend fun initializeIfNeeded(): AppResult<Unit>
}
