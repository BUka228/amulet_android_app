package com.example.amulet.shared.domain.notifications

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.user.usecase.ObserveCurrentUserUseCase
import kotlinx.coroutines.flow.first

class SyncPushTokenUseCase(
    private val repository: NotificationsRepository,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase
) {
    /**
     * Обновляет push-токен на бэкенде с учётом пользовательских согласий.
     *
     * @param token null — удалить токен; непустое значение — зарегистрировать/обновить.
     */
    suspend operator fun invoke(token: String?): AppResult<Unit> {
        val currentUser = observeCurrentUserUseCase().first()
        val notificationsAllowed = currentUser?.consents?.notifications ?: false
        return repository.syncPushToken(token, notificationsAllowed)
    }
}
