package com.example.amulet.core.sync.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackoffPolicyTest {

    @Test
    fun `first retry uses base delay`() {
        val policy = BackoffPolicy(
            baseDelayMillis = 1_000L,
            maxDelayMillis = 32_000L,
            jitterRatioMin = 1.0,
            jitterRatioMax = 1.0
        )

        val delay = policy.nextDelayMillis(retryCount = 0)

        assertEquals(1_000L, delay)
    }

    @Test
    fun `delay grows exponentially until capped`() {
        val policy = BackoffPolicy(
            baseDelayMillis = 1_000L,
            maxDelayMillis = 8_000L,
            jitterRatioMin = 1.0,
            jitterRatioMax = 1.0
        )

        assertEquals(1_000L, policy.nextDelayMillis(0))
        assertEquals(2_000L, policy.nextDelayMillis(1))
        assertEquals(4_000L, policy.nextDelayMillis(2))
        assertEquals(8_000L, policy.nextDelayMillis(3))
        assertEquals(8_000L, policy.nextDelayMillis(6))
    }

    @Test
    fun `jitter keeps value within expected range`() {
        val policy = BackoffPolicy(
            baseDelayMillis = 2_000L,
            maxDelayMillis = 32_000L,
            jitterRatioMin = 0.5,
            jitterRatioMax = 1.5
        )

        val delay = policy.nextDelayMillis(retryCount = 1)

        assertTrue(delay in 2_000L..6_000L)
    }
}
