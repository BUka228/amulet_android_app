package com.example.amulet.data.practices

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.practices.PracticesRepository
import com.github.michaelbull.result.Ok
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PracticesRepositoryImpl @Inject constructor() : PracticesRepository {
    override suspend fun loadPractices(): AppResult<Unit> = Ok(Unit)
}
