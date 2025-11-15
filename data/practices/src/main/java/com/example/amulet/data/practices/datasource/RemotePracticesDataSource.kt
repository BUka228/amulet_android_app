package com.example.amulet.data.practices.datasource

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.github.michaelbull.result.Ok
import javax.inject.Inject

interface RemotePracticesDataSource {
    suspend fun refreshCatalog(): AppResult<Unit>
}

class RemotePracticesDataSourceStub @Inject constructor() : RemotePracticesDataSource {
    override suspend fun refreshCatalog(): AppResult<Unit> = Ok(Unit)
}
