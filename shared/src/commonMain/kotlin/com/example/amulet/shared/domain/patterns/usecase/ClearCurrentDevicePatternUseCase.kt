package com.example.amulet.shared.domain.patterns.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.patterns.PatternPlaybackService

/**
 * Use case для принудительной очистки текущего подключенного амулета
 * (отправка одиночной команды ClearAll).
 */
class ClearCurrentDevicePatternUseCase(
    private val playbackService: PatternPlaybackService,
) {
    suspend operator fun invoke(): AppResult<Unit> = playbackService.clearCurrentDevice()
}
