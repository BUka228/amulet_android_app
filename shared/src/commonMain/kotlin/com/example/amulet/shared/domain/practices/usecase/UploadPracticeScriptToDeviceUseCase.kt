package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.devices.model.AmuletCommand
import com.example.amulet.shared.domain.devices.repository.DevicesRepository
import com.example.amulet.shared.domain.practices.model.Practice
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.onFailure
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UploadPracticeScriptToDeviceUseCase(
    private val devicesRepository: DevicesRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {

    suspend operator fun invoke(practice: Practice): AppResult<Unit> = withContext(dispatcher) {
        try {
            val practiceScript = practice.script ?: return@withContext Ok(Unit)
            val patternIds = practiceScript.steps
                .sortedBy { it.order }
                .mapNotNull { step -> step.patternId }

            if (patternIds.isEmpty()) return@withContext Ok(Unit)

            val beginResult = devicesRepository.sendCommand(
                AmuletCommand.BeginPracticeScript(practiceId = practice.id)
            )
            beginResult.onFailure { return@withContext Err(it) }

            patternIds.forEachIndexed { index, patternId ->
                val addResult = devicesRepository.sendCommand(
                    AmuletCommand.AddPracticeStep(
                        practiceId = practice.id,
                        order = index + 1,
                        patternId = patternId,
                    )
                )
                addResult.onFailure { return@withContext Err(it) }
            }

            val commitResult = devicesRepository.sendCommand(
                AmuletCommand.CommitPracticeScript(practiceId = practice.id)
            )
            commitResult.onFailure { return@withContext Err(it) }

            Ok(Unit)
        } catch (e: Exception) {
            Err(AppError.Unknown)
        }
    }
}
