package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.core.AppResult

interface SendHugUseCase {
    suspend operator fun invoke(): AppResult<Unit>
}

class DefaultSendHugUseCase(
    private val repository: HugsRepository
) : SendHugUseCase {
    override suspend fun invoke(): AppResult<Unit> = repository.sendHug()
}
