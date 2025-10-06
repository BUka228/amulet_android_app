package com.example.amulet.data.rules

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.rules.RulesRepository
import com.github.michaelbull.result.Ok
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RulesRepositoryImpl @Inject constructor() : RulesRepository {
    override suspend fun syncRules(): AppResult<Unit> = Ok(Unit)
}
