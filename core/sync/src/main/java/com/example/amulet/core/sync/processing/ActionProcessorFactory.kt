package com.example.amulet.core.sync.processing

import com.example.amulet.core.database.entity.OutboxActionType

interface ActionProcessorFactory {
    fun get(type: OutboxActionType): ActionProcessor
}
