package com.example.amulet.shared.domain.initialization.usecase

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.logging.Logger
import com.example.amulet.shared.domain.courses.CoursesRepository
import com.example.amulet.shared.domain.patterns.PatternsRepository
import com.example.amulet.shared.domain.practices.PracticesRepository
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess

/**
 * UseCase для локального сидирования данных при инициализации приложения.
 * Выполняет сидирование паттернов (как пресеты), практик и курсов без обращения к серверу.
 */
class SeedLocalDataUseCase(
    private val practicesRepository: PracticesRepository,
    private val patternsRepository: PatternsRepository,
    private val coursesRepository: CoursesRepository
) {
    
    suspend operator fun invoke(): AppResult<Unit> {
        Logger.d("Начало локального сидирования данных при инициализации приложения", "SeedLocalDataUseCase")
        
        return try {
            // 1) Локальное сидирование паттернов (сначала, так как практики на них ссылаются)
            val patternsResult = patternsRepository.seedLocalData()
            patternsResult.onFailure { error ->
                Logger.e("Ошибка локального сидирования паттернов: $error", tag = "SeedLocalDataUseCase")
                return Err(error)
            }
            Logger.d("Паттерны успешно засеяны локально", "SeedLocalDataUseCase")
            
            // 2) Локальное сидирование практик
            val practicesResult = practicesRepository.seedLocalData()
            practicesResult.onFailure { error ->
                Logger.e("Ошибка локального сидирования практик: $error", tag = "SeedLocalDataUseCase")
                return Err(error)
            }
            Logger.d("Практики успешно засеяны локально", "SeedLocalDataUseCase")
            
            // 3) Локальное сидирование курсов
            val coursesResult = coursesRepository.seedLocalData()
            coursesResult.onFailure { error ->
                Logger.e("Ошибка локального сидирования курсов: $error", tag = "SeedLocalDataUseCase")
                return Err(error)
            }
            Logger.d("Курсы успешно засеяны локально", "SeedLocalDataUseCase")
            
            Logger.d("Локальное сидирование данных завершено успешно", "SeedLocalDataUseCase")
            Ok(Unit)
            
        } catch (e: Exception) {
            Logger.e("Критическая ошибка при локальном сидировании данных: $e", throwable = e, tag = "SeedLocalDataUseCase")
            Err(AppError.Unknown)
        }
    }
}
