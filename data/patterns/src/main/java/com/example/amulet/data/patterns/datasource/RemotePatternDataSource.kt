package com.example.amulet.data.patterns.datasource

import com.example.amulet.core.network.dto.pattern.PatternDto
import com.example.amulet.core.network.dto.pattern.PatternMarkersDto
import com.example.amulet.shared.core.AppResult

/**
 * Удаленный источник данных для паттернов.
 * Инкапсулирует работу с PatternsApiService.
 */
interface RemotePatternDataSource {
    
    /**
     * Получить все паттерны пользователя с сервера.
     */
    suspend fun getOwnPatterns(): AppResult<List<PatternDto>>
    
    /**
     * Получить публичные паттерны с сервера.
     */
    suspend fun getPublicPatterns(
        hardwareVersion: Int? = null,
        kind: String? = null,
        tags: String? = null
    ): AppResult<List<PatternDto>>
    
    /**
     * Получить паттерн по ID с сервера.
     */
    suspend fun getPattern(patternId: String): AppResult<PatternDto>
    
    /**
     * Создать паттерн на сервере.
     */
    suspend fun createPattern(
        kind: String,
        specJson: String,
        title: String?,
        description: String?,
        tags: List<String>?,
        public: Boolean?,
        hardwareVersion: Int
    ): AppResult<PatternDto>
    
    /**
     * Обновить паттерн на сервере.
     */
    suspend fun updatePattern(
        patternId: String,
        version: Int,
        kind: String?,
        specJson: String?,
        title: String?,
        description: String?,
        tags: List<String>?,
        public: Boolean?
    ): AppResult<PatternDto>
    
    /**
     * Удалить паттерн на сервере.
     */
    suspend fun deletePattern(patternId: String): AppResult<Unit>
    
    /**
     * Расшарить паттерн с пользователем.
     */
    suspend fun sharePattern(
        patternId: String,
        toUserId: String?,
        pairId: String?
    ): AppResult<Unit>

    /**
     * Получить сегменты паттерна по parentPatternId с сервера.
     */
    suspend fun getPatternSegments(patternId: String): AppResult<List<PatternDto>>

    /**
     * Пересохранить сегменты паттерна на сервере.
     */
    suspend fun upsertPatternSegments(
        patternId: String,
        segments: List<PatternDto>
    ): AppResult<List<PatternDto>>

    /**
     * Получить маркеры таймлайна для паттерна с сервера.
     */
    suspend fun getPatternMarkers(patternId: String): AppResult<PatternMarkersDto?>

    /**
     * Сохранить или обновить маркеры таймлайна паттерна на сервере.
     */
    suspend fun upsertPatternMarkers(markers: PatternMarkersDto): AppResult<PatternMarkersDto>

    /**
     * Удалить маркеры таймлайна паттерна на сервере.
     */
    suspend fun deletePatternMarkers(patternId: String): AppResult<Unit>
}
