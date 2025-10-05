package com.example.amulet.shared.domain.hugs

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

private class FakeHugsRepository : HugsRepository {
    override suspend fun sendHug(): Result<Unit> = Result.success(Unit)
}

class DefaultSendHugUseCaseTest {
    private val repository = FakeHugsRepository()
    private val useCase = DefaultSendHugUseCase(repository)

    @Test
    fun `invoke delegates to repository`() = runTest {
        val result = useCase()
        assertTrue(result.isSuccess)
    }
}
