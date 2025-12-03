package com.example.amulet.shared.di

import com.example.amulet.shared.domain.auth.usecase.EnableGuestModeUseCase
import com.example.amulet.shared.domain.auth.usecase.SignInUseCase
import com.example.amulet.shared.domain.auth.usecase.SignInWithGoogleUseCase
import com.example.amulet.shared.domain.auth.usecase.SignOutUseCase
import com.example.amulet.shared.domain.auth.usecase.SignUpUseCase
import com.example.amulet.shared.domain.courses.usecase.*
import com.example.amulet.shared.domain.devices.usecase.*
import com.example.amulet.shared.domain.hugs.DefaultSendHugUseCase
import com.example.amulet.shared.domain.hugs.ObserveHugsForPairUseCase
import com.example.amulet.shared.domain.hugs.ObserveHugsForUserUseCase
import com.example.amulet.shared.domain.hugs.ObservePairEmotionsUseCase
import com.example.amulet.shared.domain.hugs.ObservePairQuickRepliesUseCase
import com.example.amulet.shared.domain.hugs.ObservePairUseCase
import com.example.amulet.shared.domain.hugs.ObservePairsUseCase
import com.example.amulet.shared.domain.hugs.SendHugUseCase
import com.example.amulet.shared.domain.hugs.UpdateHugStatusUseCase
import com.example.amulet.shared.domain.hugs.UpdatePairEmotionsUseCase
import com.example.amulet.shared.domain.hugs.UpdatePairMemberSettingsUseCase
import com.example.amulet.shared.domain.hugs.UpdatePairQuickRepliesUseCase
import com.example.amulet.shared.domain.hugs.ExecuteRemoteHugCommandUseCase
import com.example.amulet.shared.domain.hugs.SetHugsDndEnabledUseCase
import com.example.amulet.shared.domain.hugs.BlockPairUseCase
import com.example.amulet.shared.domain.hugs.SendQuickReplyByGestureUseCase
import com.example.amulet.shared.domain.hugs.GetSecretCodesUseCase
import com.example.amulet.shared.domain.hugs.GetHugByIdUseCase
import com.example.amulet.shared.domain.hugs.SyncHugsUseCase
import com.example.amulet.shared.domain.hugs.SyncPairsUseCase
import com.example.amulet.shared.domain.hugs.InvitePairUseCase
import com.example.amulet.shared.domain.hugs.AcceptPairUseCase
import com.example.amulet.shared.domain.notifications.SyncPushTokenUseCase
import com.example.amulet.shared.domain.initialization.usecase.SeedLocalDataUseCase
import com.example.amulet.shared.domain.patterns.PatternPlaybackService
import com.example.amulet.shared.domain.patterns.compiler.PatternCompiler
import com.example.amulet.shared.domain.patterns.compiler.PatternCompilerImpl
import com.example.amulet.shared.domain.patterns.usecase.*
import com.example.amulet.shared.domain.practices.PracticeSessionManager
import com.example.amulet.shared.domain.practices.PracticeSessionManagerImpl
import com.example.amulet.shared.domain.practices.PracticeScriptOrchestrator
import com.example.amulet.shared.domain.practices.PracticeScriptOrchestratorImpl
import com.example.amulet.shared.domain.practices.usecase.*
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
    
    // Initialization UseCases
    factory { SeedLocalDataUseCase(get(), get(), get()) }
    
    // User UseCases
    factory { ObserveCurrentUserUseCase(get(), get()) }

    // Hugs UseCases
    factory<SendHugUseCase> { DefaultSendHugUseCase(get(), get()) }
    factory { ObserveHugsForPairUseCase(get()) }
    factory { ObserveHugsForUserUseCase(get()) }
    factory { UpdateHugStatusUseCase(get()) }
    factory { GetHugByIdUseCase(get()) }
    factory { SyncHugsUseCase(get()) }
    factory { ExecuteRemoteHugCommandUseCase(get(), get(), get(), get(), get(), get()) }
    factory { SetHugsDndEnabledUseCase(get(), get()) }
    factory { BlockPairUseCase(get()) }
    factory { SendQuickReplyByGestureUseCase(get(), get()) }
    factory { GetSecretCodesUseCase(get()) }
    factory { ObservePairsUseCase(get()) }
    factory { ObservePairUseCase(get()) }
    factory { ObservePairEmotionsUseCase(get()) }
    factory { UpdatePairEmotionsUseCase(get()) }
    factory { ObservePairQuickRepliesUseCase(get()) }
    factory { UpdatePairQuickRepliesUseCase(get()) }
    factory { UpdatePairMemberSettingsUseCase(get()) }
    factory { InvitePairUseCase(get()) }
    factory { AcceptPairUseCase(get()) }
    factory { SyncPairsUseCase(get()) }

    // Notifications UseCases
    factory { SyncPushTokenUseCase(get(), get()) }

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
    factory { ObserveDeviceSessionStatusUseCase(get(), get()) }
    factory { UpdateDeviceSettingsUseCase(get()) }

    // OTA UseCases
    factory { CheckFirmwareUpdateUseCase(get()) }
    factory { StartBleOtaUpdateUseCase(get()) }
    factory { StartWifiOtaUpdateUseCase(get()) }
    factory { CancelOtaUpdateUseCase(get()) }
    
    // Patterns Compiler / Playback
    single<PatternCompiler> { PatternCompilerImpl() }
    single { PatternPlaybackService(get(), get(), get()) }
    
    // Patterns UseCases
    factory { PatternValidator() }
    factory { CreatePatternUseCase(get(), get()) }
    factory { UpdatePatternUseCase(get(), get()) }
    factory { DeletePatternUseCase(get()) }
    factory { GetPatternsStreamUseCase(get()) }
    factory { GetPresetsUseCase(get()) }
    factory { GetPatternByIdUseCase(get()) }
    factory { EnsurePatternLoadedUseCase(get()) }
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
    factory { PreviewPatternOnDeviceUseCase(get()) }
    factory { ClearCurrentDevicePatternUseCase(get()) }

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
    factory { GetScheduledSessionsStreamUseCase(get(), get()) }
    factory { GetScheduledSessionsForDateRangeUseCase(get(), get()) }
    factory { RefreshPracticesCatalogUseCase(get()) }
    factory { StartPracticeUseCase(get()) }
    factory { StopSessionUseCase(get()) }
    factory { GetUserPreferencesStreamUseCase(get()) }
    factory { UpdateUserPreferencesUseCase(get()) }
    factory { GetRecommendationsStreamUseCase(get()) }
    factory { UpsertPracticeScheduleUseCase(get()) }
    factory { GetScheduleByPracticeIdUseCase(get()) }
    factory { DeletePracticeScheduleUseCase(get()) }
    factory { DeleteSchedulesForCourseUseCase(get()) }
    factory { SkipScheduledSessionUseCase(get()) }
    factory { LogMoodSelectionUseCase(get()) }
    factory { GetPracticeScriptUseCase(get()) }
    factory { UpdateSessionFeedbackUseCase(get(), get()) }
    factory { UpdateSessionMoodBeforeUseCase(get(), get()) }

    // Practice script orchestrator
    factory<PracticeScriptOrchestrator> { PracticeScriptOrchestratorImpl(get(), get()) }

    // Practices Manager
    factory<PracticeSessionManager> {
        PracticeSessionManagerImpl(
            startPractice = get(),
            stopSessionUseCase = get(),
            getActiveSessionStreamUseCase = get(),
            getPracticeById = get(),
            clearCurrentDevicePattern = get(),
            scriptOrchestrator = get(),
        )
    }

    // Courses UseCases
    factory { GetCoursesStreamUseCase(get()) }
    factory { GetCourseByIdUseCase(get()) }
    factory { GetCourseItemsStreamUseCase(get()) }
    factory { GetCourseModulesStreamUseCase(get()) }
    factory { GetCourseProgressStreamUseCase(get()) }
    factory { GetAllCoursesProgressStreamUseCase(get()) }
    factory { GetCoursesByPracticeIdUseCase(get()) }
    factory { RefreshCoursesUseCase(get()) }
    factory { RefreshCoursesCatalogUseCase(get()) }
    factory { StartCourseUseCase(get()) }
    factory { ContinueCourseUseCase(get()) }
    factory { CompleteCourseItemUseCase(get()) }
    factory { ResetCourseProgressUseCase(get()) }
    factory { SearchCoursesUseCase(get()) }
    factory { CheckItemUnlockUseCase(get()) }
    factory { GetUnlockedItemsUseCase(get(), get()) }
    factory { GetNextCourseItemUseCase(get(), get()) }
    factory { CreateScheduleForCourseUseCase() }
    factory { EnrollCourseUseCase(get(), get(), get()) }

    // Complete practice session (нужен доступ к практикам и курсам для источника FromCourse)
    factory { CompletePracticeSessionUseCase(get(), get()) }
}

fun sharedKoinModules(): List<Module> = listOf(sharedModule)
