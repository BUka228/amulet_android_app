package com.example.amulet.core.sync.processing

import com.example.amulet.core.database.entity.OutboxActionType
import javax.inject.Inject

class DefaultActionProcessorFactory @Inject constructor(
    private val processors: Map<@JvmSuppressWildcards OutboxActionType, @JvmSuppressWildcards ActionProcessor>
) : ActionProcessorFactory {

    override fun get(type: OutboxActionType): ActionProcessor =
        processors[type] ?: error("ActionProcessor is not registered for type: $type")
}
