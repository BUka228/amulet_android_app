package com.example.amulet.shared.domain.notifications.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.mapSuccess
import com.example.amulet.shared.domain.notifications.model.PushTokenRegistration
import com.example.amulet.shared.domain.notifications.repository.NotificationsRepository

class RegisterPushTokenUseCase(
    private val repository: NotificationsRepository
) {
    suspend operator fun invoke(registration: PushTokenRegistration): AppResult<Unit> =
        repository.registerPushToken(registration).mapSuccess { }
}
