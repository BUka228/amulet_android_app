package com.example.amulet.shared.domain.patterns.usecase

import com.example.amulet.shared.core.logging.Logger
import com.example.amulet.shared.domain.devices.model.DeviceId
import com.example.amulet.shared.domain.devices.repository.DevicesRepository
import com.example.amulet.shared.domain.patterns.compiler.PatternCompiler
import com.example.amulet.shared.domain.patterns.model.PatternSpec
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * UseCase для предпросмотра паттерна на реальном устройстве.
 */
class PreviewPatternOnDeviceUseCase(
    private val compiler: PatternCompiler,
    private val devicesRepository: DevicesRepository
) {
    suspend operator fun invoke(
        spec: PatternSpec,
        deviceId: DeviceId
    ): Flow<PreviewProgress> = flow {
        Logger.d("Начало предпросмотра паттерна на устройстве: ${spec.type}, deviceId: $deviceId", "PreviewPatternOnDeviceUseCase")
        emit(PreviewProgress.Compiling)
        
        // Получение информации об устройстве
        devicesRepository.getDevice(deviceId)
            .onSuccess { device ->
                Logger.d("Устройство найдено: ${device.name}, hardware: ${device.hardwareVersion}", "PreviewPatternOnDeviceUseCase")
                // Компиляция
                val plan = compiler.compile(
                    spec = spec,
                    hardwareVersion = device.hardwareVersion,
                    firmwareVersion = device.firmwareVersion ?: "1.0.0"
                )
                
                emit(PreviewProgress.Uploading(0))
                Logger.d("Компиляция завершена, команд: ${plan.commands.size}", "PreviewPatternOnDeviceUseCase")
                
                // TODO: Отправка на устройство через DevicesRepository
                // Когда DevicesRepository будет иметь метод sendCommandPlan,
                // здесь нужно будет вызвать его и обрабатывать прогресс
                
                // Пока просто эмитим успех
                emit(PreviewProgress.Playing)
                Logger.d("Предпросмотр начат", "PreviewPatternOnDeviceUseCase")
            }
            .onFailure { error ->
                emit(PreviewProgress.Failed(Exception("Device not found: $error")))
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
