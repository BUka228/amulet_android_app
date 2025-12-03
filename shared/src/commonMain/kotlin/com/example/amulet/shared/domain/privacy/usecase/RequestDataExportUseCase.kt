package com.example.amulet.shared.domain.privacy.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.privacy.PrivacyRepository

/**
 * Запрашивает экспорт данных пользователя.
 */
class RequestDataExportUseCase(
    private val privacyRepository: PrivacyRepository,
) {
    suspend operator fun invoke(): AppResult<Unit> =
        privacyRepository.requestDataExport()
}
