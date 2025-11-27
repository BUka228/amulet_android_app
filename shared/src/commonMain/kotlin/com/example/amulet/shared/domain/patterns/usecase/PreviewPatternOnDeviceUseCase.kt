package com.example.amulet.shared.domain.patterns.usecase

import com.example.amulet.shared.core.logging.Logger
import com.example.amulet.shared.domain.devices.model.DeviceId
import com.example.amulet.shared.domain.patterns.PatternPlaybackService
import com.example.amulet.shared.domain.patterns.model.PatternSpec
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.collect

/**
 * UseCase для предпросмотра паттерна на реальном устройстве.
 */
class PreviewPatternOnDeviceUseCase(
    private val playbackService: PatternPlaybackService
) {
    suspend operator fun invoke(
        spec: PatternSpec,
        deviceId: DeviceId
    ): Flow<PreviewProgress> = flow {
        Logger.d("Начало предпросмотра паттерна на устройстве: ${spec.type}, deviceId: $deviceId", "PreviewPatternOnDeviceUseCase")
        emit(PreviewProgress.Compiling)
        try {
            // Через сервис воспроизведения паттернов
            playbackService.playOnDevice(spec, deviceId)
                .onSuccess {
                    emit(PreviewProgress.Playing)
                    Logger.d("Предпросмотр начат", "PreviewPatternOnDeviceUseCase")
                }
                .onFailure { error ->
                    emit(PreviewProgress.Failed(Exception("Pattern playback failed: $error")))
                }
        } catch (e: Exception) {
            emit(PreviewProgress.Failed(e))
        }
    }
}

/**
 * Прогресс предпросмотра паттерна.
 */
sealed interface PreviewProgress {
    data object Compiling : PreviewProgress
    data class Uploading(val percent: Int) : PreviewProgress
    data object Playing : PreviewProgress
    data class Failed(val cause: Throwable?) : PreviewProgress
}
