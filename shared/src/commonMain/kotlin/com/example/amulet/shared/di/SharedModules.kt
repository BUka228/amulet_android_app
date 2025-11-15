package com.example.amulet.shared.di

import com.example.amulet.shared.domain.auth.usecase.EnableGuestModeUseCase
import com.example.amulet.shared.domain.auth.usecase.SignInUseCase
import com.example.amulet.shared.domain.auth.usecase.SignInWithGoogleUseCase
import com.example.amulet.shared.domain.auth.usecase.SignOutUseCase
import com.example.amulet.shared.domain.auth.usecase.SignUpUseCase
import com.example.amulet.shared.domain.devices.usecase.*
import com.example.amulet.shared.domain.hugs.DefaultSendHugUseCase
import com.example.amulet.shared.domain.hugs.SendHugUseCase
import com.example.amulet.shared.domain.patterns.compiler.PatternCompiler
import com.example.amulet.shared.domain.patterns.compiler.PatternCompilerImpl
import com.example.amulet.shared.domain.patterns.usecase.*
import com.example.amulet.shared.domain.practices.usecase.*
import com.example.amulet.shared.domain.courses.usecase.*
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

    // Devices UseCases (локальная работа без серверной привязки)
    factory { ObserveDevicesUseCase(get()) }
    factory { GetDeviceUseCase(get()) }
    factory { AddDeviceUseCase(get()) }
    factory { RemoveDeviceUseCase(get()) }
    factory { ScanForDevicesUseCase(get()) }
    factory { ConnectToDeviceUseCase(get()) }
    factory { DisconnectFromDeviceUseCase(get()) }
    factory { ObserveConnectionStateUseCase(get()) }
    factory { ObserveConnectedDeviceStatusUseCase(get()) }
    factory { UpdateDeviceSettingsUseCase(get()) }

    // OTA UseCases
    factory { CheckFirmwareUpdateUseCase(get()) }
    factory { StartBleOtaUpdateUseCase(get()) }
    factory { StartWifiOtaUpdateUseCase(get()) }
    factory { CancelOtaUpdateUseCase(get()) }
    
    // Patterns Compiler
    single<PatternCompiler> { PatternCompilerImpl() }
    
    // Patterns UseCases
    factory { PatternValidator() }
    factory { CreatePatternUseCase(get(), get()) }
    factory { UpdatePatternUseCase(get(), get()) }
    factory { DeletePatternUseCase(get()) }
    factory { GetPatternsStreamUseCase(get()) }
    factory { GetPatternByIdUseCase(get()) }
    factory { ObserveMyPatternsUseCase(get()) }
    factory { SyncPatternsUseCase(get()) }
    factory { PublishPatternUseCase(get()) }
    factory { SharePatternUseCase(get()) }
    factory { AddTagToPatternUseCase(get()) }
    factory { RemoveTagFromPatternUseCase(get()) }
    factory { GetAllTagsUseCase(get()) }
    factory { CreateTagsUseCase(get()) }
    factory { SetPatternTagsUseCase(get()) }
    factory { DeleteTagsUseCase(get()) }
    factory { PreviewPatternOnDeviceUseCase(get(), get()) }

    // Practices UseCases
    factory { GetPracticesStreamUseCase(get()) }
    factory { GetPracticeByIdUseCase(get()) }
    factory { GetCategoriesStreamUseCase(get()) }
    factory { GetFavoritesStreamUseCase(get()) }
    factory { SearchPracticesUseCase(get()) }
    factory { RefreshPracticesUseCase(get()) }
    factory { SetFavoritePracticeUseCase(get()) }
    factory { GetActiveSessionStreamUseCase(get()) }
    factory { GetSessionsHistoryStreamUseCase(get()) }
    factory { StartPracticeUseCase(get()) }
    factory { PauseSessionUseCase(get()) }
    factory { ResumeSessionUseCase(get()) }
    factory { StopSessionUseCase(get()) }
    factory { GetUserPreferencesStreamUseCase(get()) }
    factory { UpdateUserPreferencesUseCase(get()) }
    factory { GetRecommendationsStreamUseCase(get()) }

    // Courses UseCases
    factory { GetCoursesStreamUseCase(get()) }
    factory { GetCourseByIdUseCase(get()) }
    factory { GetCourseItemsStreamUseCase(get()) }
    factory { GetCourseProgressStreamUseCase(get()) }
    factory { RefreshCoursesUseCase(get()) }
    factory { StartCourseUseCase(get()) }
    factory { ContinueCourseUseCase(get()) }
    factory { CompleteCourseItemUseCase(get()) }
    factory { ResetCourseProgressUseCase(get()) }
}

fun sharedKoinModules(): List<Module> = listOf(sharedModule)
