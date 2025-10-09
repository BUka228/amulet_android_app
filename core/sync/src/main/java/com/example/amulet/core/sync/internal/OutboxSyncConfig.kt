package com.example.amulet.core.sync.internal

data class OutboxSyncConfig(
    val maxActionsPerSync: Int,
    val inFlightTimeoutMillis: Long,
    val baseBackoffMillis: Long,
    val maxBackoffMillis: Long
)
