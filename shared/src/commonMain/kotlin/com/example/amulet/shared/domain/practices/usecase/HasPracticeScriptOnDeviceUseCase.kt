package com.example.amulet.shared.domain.practices.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.devices.model.AmuletCommand
import com.example.amulet.shared.domain.devices.repository.DevicesRepository
import com.example.amulet.shared.domain.practices.model.PracticeId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HasPracticeScriptOnDeviceUseCase(
    private val devicesRepository: DevicesRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {

    /**
     * Проверяет, существует ли скрипт практики на текущем подключенном устройстве.
     *
     * По спецификации BLE: любой ответ OK:HAS_PRACTICE трактуем как "есть",
     * любые ERROR:HAS_PRACTICE:... — как "нет" (перезагрузка скрипта при необходимости).
     */
    suspend operator fun invoke(practiceId: PracticeId): Boolean = withContext(dispatcher) {
        try {
            val commandResult: AppResult<Unit> = devicesRepository.sendCommand(
                AmuletCommand.HasPracticeScript(practiceId = practiceId)
            )
            val error = commandResult.component2()
            error == null
        } catch (_: Exception) {
            false
        }
    }
}
