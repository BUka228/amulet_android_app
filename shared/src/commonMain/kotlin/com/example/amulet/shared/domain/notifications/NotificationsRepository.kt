package com.example.amulet.shared.domain.notifications

import com.example.amulet.shared.core.AppResult

/**
 * Репозиторий уведомлений.
 * Отвечает за синхронизацию push-токена текущего пользователя с бэкендом.
 */
interface NotificationsRepository {

    /**
     * Синхронизирует актуальный push-токен.
     *
     * @param token null означает, что токен нужно отозвать/удалить на бэкенде.
     * @param notificationsAllowed разрешены ли уведомления в пользовательских согласиях.
     */
    suspend fun syncPushToken(token: String?, notificationsAllowed: Boolean): AppResult<Unit>
}
