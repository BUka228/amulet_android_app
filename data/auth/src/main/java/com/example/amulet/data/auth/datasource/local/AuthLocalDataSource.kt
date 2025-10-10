package com.example.amulet.data.auth.datasource.local

/**
 * Локальный источник данных для операций авторизации.
 * Отвечает за очистку пользовательских данных при выходе из аккаунта.
 */
interface AuthLocalDataSource {

    /**
     * Полностью очищает локальное хранилище данных пользователя.
     */
    suspend fun clearAll()
}
