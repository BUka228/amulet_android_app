package com.example.amulet.data.hugs

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.hugs.HugsRepository
import com.github.michaelbull.result.Ok
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HugsRepositoryImpl @Inject constructor() : HugsRepository {
    override suspend fun sendHug(): AppResult<Unit> = Ok(Unit)
}
