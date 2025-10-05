package com.example.amulet.shared.domain.patterns

import com.example.amulet.shared.core.AppResult

interface PatternsRepository {
    suspend fun syncPatterns(): AppResult<Unit>
}
