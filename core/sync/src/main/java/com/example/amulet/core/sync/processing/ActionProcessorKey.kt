package com.example.amulet.core.sync.processing

import com.example.amulet.core.database.entity.OutboxActionType
import dagger.MapKey

@MapKey
annotation class ActionProcessorKey(val value: OutboxActionType)
