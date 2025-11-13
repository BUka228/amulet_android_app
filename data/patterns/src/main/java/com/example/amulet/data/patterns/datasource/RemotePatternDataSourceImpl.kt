package com.example.amulet.data.patterns.datasource

import com.example.amulet.core.network.NetworkExceptionMapper
import com.example.amulet.core.network.dto.pattern.PatternCreateRequestDto
import com.example.amulet.core.network.dto.pattern.PatternDto
import com.example.amulet.core.network.dto.pattern.PatternShareRequestDto
import com.example.amulet.core.network.dto.pattern.PatternSpecDto
import com.example.amulet.core.network.dto.pattern.PatternUpdateRequestDto
import com.example.amulet.core.network.service.PatternsApiService
import com.example.amulet.core.network.safeApiCall
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.logging.Logger
import com.github.michaelbull.result.map
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Реализация удаленного источника данных для паттернов.
 */
class RemotePatternDataSourceImpl @Inject constructor(
    private val apiService: PatternsApiService,
    private val exceptionMapper: NetworkExceptionMapper,
    private val json: Json
) : RemotePatternDataSource {
    
    override suspend fun getOwnPatterns(): AppResult<List<PatternDto>> {
        Logger.d("Получение паттернов пользователя с сервера", "RemotePatternDataSourceImpl")
        val result = safeApiCall(exceptionMapper) { apiService.getOwnPatterns() }
            .map { it.items }
        Logger.d("Получено паттернов: ${result.component1()?.size ?: 0}", "RemotePatternDataSourceImpl")
        return result
    }
    
    override suspend fun getPublicPatterns(
        hardwareVersion: Int?,
        kind: String?,
        tags: String?
    ): AppResult<List<PatternDto>> {
        Logger.d("Получение публичных паттернов с сервера", "RemotePatternDataSourceImpl")
        val result = safeApiCall(exceptionMapper) { 
            apiService.getPublicPatterns(
                hardwareVersion = hardwareVersion,
                kind = kind,
                tags = tags
            ) 
        }.map { it.items }
        Logger.d("Получено публичных паттернов: ${result.component1()?.size ?: 0}", "RemotePatternDataSourceImpl")
        return result
    }
    
    override suspend fun getPattern(patternId: String): AppResult<PatternDto> {
        Logger.d("Получение паттерна с сервера: $patternId", "RemotePatternDataSourceImpl")
        val result = safeApiCall(exceptionMapper) { apiService.getPattern(patternId) }
            .map { it.pattern }
        Logger.d("Получение паттерна завершено: $patternId", "RemotePatternDataSourceImpl")
        return result
    }
    
    override suspend fun createPattern(
        kind: String,
        specJson: String,
        title: String?,
        description: String?,
        tags: List<String>?,
        public: Boolean?,
        hardwareVersion: Int
    ): AppResult<PatternDto> {
        Logger.d("Создание паттерна на сервере", "RemotePatternDataSourceImpl")
        val result = safeApiCall(exceptionMapper) { 
            val spec = json.decodeFromString<PatternSpecDto>(specJson)
            apiService.createPattern(
                PatternCreateRequestDto(
                    kind = kind,
                    spec = spec,
                    title = title,
                    description = description,
                    tags = tags,
                    public = public,
                    hardwareVersion = hardwareVersion
                )
            ) 
        }.map { it.pattern }
        Logger.d("Создание паттерна на сервере завершено", "RemotePatternDataSourceImpl")
        return result
    }
    
    override suspend fun updatePattern(
        patternId: String,
        version: Int,
        kind: String?,
        specJson: String?,
        title: String?,
        description: String?,
        tags: List<String>?,
        public: Boolean?
    ): AppResult<PatternDto> {
        Logger.d("Обновление паттерна на сервере: $patternId", "RemotePatternDataSourceImpl")
        val result = safeApiCall(exceptionMapper) { 
            val spec = specJson?.let { json.decodeFromString<PatternSpecDto>(it) }
            apiService.updatePattern(
                patternId = patternId,
                request = PatternUpdateRequestDto(
                    version = version,
                    kind = kind,
                    spec = spec,
                    title = title,
                    description = description,
                    tags = tags,
                    public = public
                )
            ) 
        }.map { it.pattern }
        Logger.d("Обновление паттерна на сервере завершено: $patternId", "RemotePatternDataSourceImpl")
        return result
    }
    
    override suspend fun deletePattern(patternId: String): AppResult<Unit> {
        Logger.d("Удаление паттерна на сервере: $patternId", "RemotePatternDataSourceImpl")
        val result = safeApiCall(exceptionMapper) { apiService.deletePattern(patternId) }
            .map { Unit }
        Logger.d("Удаление паттерна на сервере завершено: $patternId", "RemotePatternDataSourceImpl")
        return result
    }
    
    override suspend fun sharePattern(
        patternId: String,
        toUserId: String?,
        pairId: String?
    ): AppResult<Unit> {
        Logger.d("Шаринг паттерна на сервере: $patternId", "RemotePatternDataSourceImpl")
        val result = safeApiCall(exceptionMapper) { 
            apiService.sharePattern(
                patternId = patternId,
                request = PatternShareRequestDto(
                    toUserId = toUserId,
                    pairId = pairId
                )
            ) 
        }.map { Unit }
        Logger.d("Шаринг паттерна на сервере завершен: $patternId", "RemotePatternDataSourceImpl")
        return result
    }
}
