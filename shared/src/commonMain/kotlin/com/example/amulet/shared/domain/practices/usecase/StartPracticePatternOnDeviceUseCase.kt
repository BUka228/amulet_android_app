package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.patterns.PatternPlaybackService
import com.example.amulet.shared.domain.practices.model.PracticeId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class StartPracticePatternOnDeviceUseCase(
    private val getPracticeById: GetPracticeByIdUseCase,
    private val getPatternById: com.example.amulet.shared.domain.patterns.usecase.GetPatternByIdUseCase,
    private val playbackService: PatternPlaybackService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    suspend operator fun invoke(
        practiceId: PracticeId,
        intensity: Double?
    ): AppResult<Unit> = withContext(dispatcher) {
        val practice = getPracticeById(practiceId).firstOrNull()
            ?: return@withContext com.github.michaelbull.result.Err(com.example.amulet.shared.core.AppError.NotFound)

        val patternId = practice.patternId
            ?: return@withContext com.github.michaelbull.result.Err(com.example.amulet.shared.core.AppError.NotFound)

        val pattern = getPatternById(patternId).firstOrNull()
            ?: return@withContext com.github.michaelbull.result.Err(com.example.amulet.shared.core.AppError.NotFound)

        val effectiveIntensity = (intensity ?: 1.0).coerceIn(0.0, 1.0)
        playbackService.playOnConnectedDevice(pattern.spec, effectiveIntensity)
    }
}
