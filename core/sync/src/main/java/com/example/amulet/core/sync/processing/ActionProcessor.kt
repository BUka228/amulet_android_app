package com.example.amulet.core.sync.processing

import com.example.amulet.core.database.entity.OutboxActionEntity
import com.example.amulet.shared.core.AppResult

fun interface ActionProcessor {
    suspend fun process(action: OutboxActionEntity): AppResult<Unit>
}
