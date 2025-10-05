package com.example.amulet.shared.domain.rules

import com.example.amulet.shared.core.AppResult

interface RulesRepository {
    suspend fun syncRules(): AppResult<Unit>
}
