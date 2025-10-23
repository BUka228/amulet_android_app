package com.example.amulet.shared.di

import com.example.amulet.shared.domain.auth.usecase.EnableGuestModeUseCase
import com.example.amulet.shared.domain.auth.usecase.SignInUseCase
import com.example.amulet.shared.domain.auth.usecase.SignInWithGoogleUseCase
import com.example.amulet.shared.domain.auth.usecase.SignOutUseCase
import com.example.amulet.shared.domain.auth.usecase.SignUpUseCase
import com.example.amulet.shared.domain.devices.usecase.*
import com.example.amulet.shared.domain.hugs.DefaultSendHugUseCase
import com.example.amulet.shared.domain.hugs.SendHugUseCase
import com.example.amulet.shared.domain.user.usecase.ObserveCurrentUserUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin модули для :shared (domain layer).
 * Предоставляет UseCase'ы, которые зависят только от интерфейсов репозиториев.
 */
private val sharedModule = module {
    // Auth UseCases
    factory { SignInUseCase(get(), get()) }
    factory { SignInWithGoogleUseCase(get(), get()) }
    factory { SignOutUseCase(get()) }
    factory { SignUpUseCase(get(), get()) }
    factory { EnableGuestModeUseCase(get()) }
    
    // User UseCases
    factory { ObserveCurrentUserUseCase(get(), get()) }

    // Hugs UseCases
    factory<SendHugUseCase> { DefaultSendHugUseCase(get()) }

    // Devices UseCases
    factory { ObserveDevicesUseCase(get()) }
    factory { GetDeviceUseCase(get()) }
    factory { ScanForPairingUseCase(get()) }
    factory { PairAndClaimDeviceUseCase(get()) }
    factory { ConnectToDeviceUseCase(get()) }
    factory { DisconnectFromDeviceUseCase(get()) }
    factory { ObserveConnectionStateUseCase(get()) }
    factory { ObserveConnectedDeviceStatusUseCase(get()) }
    factory { UpdateDeviceSettingsUseCase(get()) }
    factory { UnclaimDeviceUseCase(get()) }
    factory { SyncDevicesUseCase(get()) }

    // OTA UseCases
    factory { CheckFirmwareUpdateUseCase(get()) }
    factory { StartBleOtaUpdateUseCase(get()) }
    factory { StartWifiOtaUpdateUseCase(get()) }
    factory { CancelOtaUpdateUseCase(get()) }
}

fun sharedKoinModules(): List<Module> = listOf(sharedModule)
