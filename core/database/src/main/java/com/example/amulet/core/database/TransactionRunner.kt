package com.example.amulet.core.database

/**
 * Интерфейс для выполнения транзакций БД.
 * Инкапсулирует работу с Room, избавляя data-слой от прямой зависимости на Room API.
 */
interface TransactionRunner {
    /**
     * Выполняет блок кода в транзакции БД.
     * Если блок выбрасывает исключение, транзакция откатывается.
     * 
     * @param block Блок кода для выполнения в транзакции
     * @return Результат выполнения блока
     */
    suspend fun <R> runInTransaction(block: suspend () -> R): R
}
