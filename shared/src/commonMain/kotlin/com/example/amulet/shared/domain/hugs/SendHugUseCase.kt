package com.example.amulet.shared.domain.hugs

interface SendHugUseCase {
    suspend operator fun invoke(): Result<Unit>
}

class DefaultSendHugUseCase(
    private val repository: HugsRepository
) : SendHugUseCase {
    override suspend fun invoke(): Result<Unit> = repository.sendHug()
}
