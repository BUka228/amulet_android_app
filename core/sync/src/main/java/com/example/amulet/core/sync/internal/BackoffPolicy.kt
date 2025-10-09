package com.example.amulet.core.sync.internal

import kotlin.math.min
import kotlin.math.roundToLong
import kotlin.random.Random

class BackoffPolicy(
    private val baseDelayMillis: Long,
    private val maxDelayMillis: Long,
    private val jitterRatioMin: Double = 0.5,
    private val jitterRatioMax: Double = 1.5,
    private val random: Random = Random.Default
) {

    init {
        require(baseDelayMillis > 0) { "baseDelayMillis must be positive" }
        require(maxDelayMillis >= baseDelayMillis) { "maxDelayMillis must be >= baseDelayMillis" }
        require(jitterRatioMax >= jitterRatioMin) { "jitterRatioMax must be >= jitterRatioMin" }
    }

    fun nextDelayMillis(retryCount: Int): Long {
        val normalizedRetries = retryCount.coerceAtLeast(0)
        val multiplier = 1L shl normalizedRetries.coerceAtMost(30)
        val exponentialDelay = baseDelayMillis * multiplier
        val cappedDelay = min(exponentialDelay, maxDelayMillis)
        val jitterRange = jitterRatioMax - jitterRatioMin
        val jitterFactor = if (jitterRange == 0.0) {
            jitterRatioMin
        } else {
            jitterRatioMin + random.nextDouble() * jitterRange
        }
        val jitteredDelay = (cappedDelay.toDouble() * jitterFactor).roundToLong()
        return jitteredDelay.coerceAtLeast(1L)
    }
}
