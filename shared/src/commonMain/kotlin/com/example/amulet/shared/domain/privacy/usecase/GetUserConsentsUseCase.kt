package com.example.amulet.shared.domain.privacy.usecase

import com.example.amulet.shared.domain.privacy.PrivacyRepository
import com.example.amulet.shared.domain.privacy.model.UserConsents
import kotlinx.coroutines.flow.Flow

/**
 * Стрим текущих согласий пользователя.
 */
class GetUserConsentsUseCase(
    private val privacyRepository: PrivacyRepository,
) {
    operator fun invoke(): Flow<UserConsents> = privacyRepository.getUserConsentsStream()
}
