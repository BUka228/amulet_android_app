package com.example.amulet.shared.domain.patterns

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.devices.model.AmuletCommand
import com.example.amulet.shared.domain.devices.model.AmuletCommandPlan
import com.example.amulet.shared.domain.devices.model.DeviceId
import com.example.amulet.shared.domain.devices.repository.DevicesRepository
import com.example.amulet.shared.domain.patterns.compiler.PatternCompiler
import com.example.amulet.shared.domain.patterns.model.PatternSpec
import com.example.amulet.shared.domain.devices.usecase.ObserveConnectedDeviceStatusUseCase
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.fold
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

/**
 * Сервис воспроизведения паттернов на устройстве.
 * Инкапсулирует компиляцию PatternSpec в AmuletCommandPlan и загрузку на амулет.
 */
class PatternPlaybackService(
    private val compiler: PatternCompiler,
    private val devicesRepository: DevicesRepository,
    private val observeConnectedDeviceStatus: ObserveConnectedDeviceStatusUseCase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    /**
     * Воспроизвести паттерн на конкретном устройстве.
     */
    suspend fun playOnDevice(
        spec: PatternSpec,
        deviceId: DeviceId,
        intensity: Double = 1.0,
    ): AppResult<Unit> = withContext(dispatcher) {
        try {
            val deviceResult = devicesRepository.getDevice(deviceId)
            deviceResult.fold(
                success = { device ->
                    val plan = compiler.compile(
                        spec = spec,
                        hardwareVersion = device.hardwareVersion,
                        firmwareVersion = device.firmwareVersion,
                        intensity = intensity
                    )

                    val commandPlan = AmuletCommandPlan(
                        commands = plan.commands + AmuletCommand.ClearAll,
                        estimatedDurationMs = plan.estimatedDurationMs
                    )

                    devicesRepository.uploadCommandPlan(commandPlan, device.hardwareVersion).firstOrNull()
                    Ok(Unit)
                },
                failure = { error -> Err(error)
                }
            )
        } catch (e: Exception) {
            Err(AppError.Unknown)
        }
    }

    /**
     * Воспроизвести паттерн на текущем подключенном устройстве.
     */
    suspend fun playOnConnectedDevice(
        spec: PatternSpec,
        intensity: Double = 1.0,
    ): AppResult<Unit> = withContext(dispatcher) {
        try {
            val status = observeConnectedDeviceStatus().firstOrNull()
                ?: return@withContext Err(AppError.BleError.DeviceNotFound)

            if (!status.isOnline) {
                return@withContext Err(AppError.BleError.DeviceDisconnected)
            }

            val plan = compiler.compile(
                spec = spec,
                hardwareVersion = status.hardwareVersion,
                firmwareVersion = status.firmwareVersion,
                intensity = intensity
            )

            val commandPlan = AmuletCommandPlan(
                commands = plan.commands + AmuletCommand.ClearAll,
                estimatedDurationMs = plan.estimatedDurationMs
            )

            devicesRepository.uploadCommandPlan(commandPlan, status.hardwareVersion).firstOrNull()
            Ok(Unit)
        } catch (e: Exception) {
            Err(AppError.Unknown)
        }
    }

    /**
     * Принудительно очистить текущий подключенный амулет (отправить ClearAll).
     */
    suspend fun clearCurrentDevice(): AppResult<Unit> = withContext(dispatcher) {
        try {
            val status = observeConnectedDeviceStatus().firstOrNull()
                ?: return@withContext Err(AppError.BleError.DeviceNotFound)

            if (!status.isOnline) {
                return@withContext Err(AppError.BleError.DeviceDisconnected)
            }

            val commandPlan = AmuletCommandPlan(
                commands = listOf(AmuletCommand.ClearAll),
                estimatedDurationMs = 0L
            )

            devicesRepository.uploadCommandPlan(commandPlan, status.hardwareVersion).firstOrNull()
            Ok(Unit)
        } catch (e: Exception) {
            Err(AppError.Unknown)
        }
    }
}
