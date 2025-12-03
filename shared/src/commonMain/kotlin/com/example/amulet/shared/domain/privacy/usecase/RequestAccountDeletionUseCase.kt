package com.example.amulet.shared.domain.privacy.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.privacy.PrivacyRepository

/**
 * Запускает процесс удаления аккаунта пользователя.
 */
class RequestAccountDeletionUseCase(
    private val privacyRepository: PrivacyRepository,
) {
    suspend operator fun invoke(): AppResult<Unit> =
        privacyRepository.requestAccountDeletion()
}
