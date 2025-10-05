package com.example.amulet.shared.domain.patterns

interface PatternsRepository {
    suspend fun syncPatterns(): Result<Unit>
}
