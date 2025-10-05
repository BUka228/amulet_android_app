package com.example.amulet.shared.domain.rules

interface RulesRepository {
    suspend fun syncRules(): Result<Unit>
}
