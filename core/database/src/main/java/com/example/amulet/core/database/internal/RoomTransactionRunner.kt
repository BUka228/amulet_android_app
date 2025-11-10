package com.example.amulet.core.database.internal

import androidx.room.withTransaction
import com.example.amulet.core.database.AmuletDatabase
import com.example.amulet.core.database.TransactionRunner
import javax.inject.Inject

/**
 * Реализация TransactionRunner на основе Room.
 * Внутренний класс модуля core:database.
 */
class RoomTransactionRunner @Inject constructor(
    private val database: AmuletDatabase
) : TransactionRunner {
    
    override suspend fun <R> runInTransaction(block: suspend () -> R): R {
        return database.withTransaction(block)
    }
}
