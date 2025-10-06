package com.example.amulet.data.patterns

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.patterns.PatternsRepository
import com.github.michaelbull.result.Ok
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatternsRepositoryImpl @Inject constructor() : PatternsRepository {
    override suspend fun syncPatterns(): AppResult<Unit> = Ok(Unit)
}
