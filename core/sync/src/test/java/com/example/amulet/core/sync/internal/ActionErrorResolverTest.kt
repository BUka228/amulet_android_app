package com.example.amulet.core.sync.internal

import com.example.amulet.core.database.entity.OutboxActionEntity
import com.example.amulet.core.database.entity.OutboxActionStatus
import com.example.amulet.core.database.entity.OutboxActionType
import com.example.amulet.shared.core.AppError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ActionErrorResolverTest {

    private val policy = BackoffPolicy(
        baseDelayMillis = 1_000L,
        maxDelayMillis = 8_000L,
        jitterRatioMin = 1.0,
        jitterRatioMax = 1.0
    )
    private val resolver = ActionErrorResolver(policy)
    private val action = createAction(retryCount = 0)

    @Test
    fun `network errors trigger retry`() {
        val resolution = resolver.resolve(action, AppError.Network)

        val retry = assertIs<ActionResolution.Retry>(resolution)
        assertEquals(1_000L, retry.delayMillis)
    }

    @Test
    fun `validation errors are treated as failures`() {
        val error = AppError.Validation(mapOf("field" to "is required"))

        val resolution = resolver.resolve(action, error)

        val failed = assertIs<ActionResolution.Failed>(resolution)
        assertEquals("Validation[field=is required]", failed.errorMessage)
    }

    private fun createAction(
        id: String = "id",
        retryCount: Int = 0
    ): OutboxActionEntity = OutboxActionEntity(
        id = id,
        type = OutboxActionType.HUG_SEND,
        payloadJson = "{}",
        status = OutboxActionStatus.PENDING,
        retryCount = retryCount,
        lastError = null,
        idempotencyKey = null,
        createdAt = 0L,
        updatedAt = 0L,
        availableAt = 0L,
        priority = 0,
        targetEntityId = null
    )
}
