package com.example.amulet.shared.domain.privacy.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.privacy.PrivacyRepository
import com.example.amulet.shared.domain.privacy.model.UserConsents

/**
 * Обновление согласий пользователя.
 *
 * Реальная синхронизация сессии (UserSessionContext) остаётся ответственностью
 * data-слоя/оркестраторов, здесь только делегирование в PrivacyRepository.
 */
class UpdateUserConsentsUseCase(
    private val privacyRepository: PrivacyRepository,
) {
    suspend operator fun invoke(consents: UserConsents): AppResult<Unit> =
        privacyRepository.updateUserConsents(consents)
}
