package com.example.amulet_android_app.di

import android.app.Application
import com.example.amulet.shared.core.auth.UserSessionProvider
import com.example.amulet.shared.core.auth.UserSessionUpdater
import com.example.amulet.shared.di.sharedKoinModules
import com.example.amulet.shared.domain.auth.repository.AuthRepository
import com.example.amulet.shared.domain.auth.usecase.EnableGuestModeUseCase
import com.example.amulet.shared.domain.auth.usecase.SignInUseCase
import com.example.amulet.shared.domain.auth.usecase.SignInWithGoogleUseCase
import com.example.amulet.shared.domain.auth.usecase.SignOutUseCase
import com.example.amulet.shared.domain.auth.usecase.SignUpUseCase
import com.example.amulet.shared.domain.devices.repository.DevicesRepository
import com.example.amulet.shared.domain.devices.repository.OtaRepository
import com.example.amulet.shared.domain.devices.usecase.*
import com.example.amulet.shared.domain.hugs.HugsRepository
import com.example.amulet.shared.domain.hugs.SendHugUseCase
import com.example.amulet.shared.domain.patterns.PatternsRepository
import com.example.amulet.shared.domain.patterns.compiler.PatternCompiler
import com.example.amulet.shared.domain.patterns.usecase.*
import com.example.amulet.shared.domain.practices.PracticeSessionManager
import com.example.amulet.shared.domain.practices.PracticesRepository
import com.example.amulet.shared.domain.practices.MoodRepository
import com.example.amulet.shared.domain.practices.usecase.CompletePracticeSessionUseCase
import com.example.amulet.shared.domain.practices.usecase.GetActiveSessionStreamUseCase
import com.example.amulet.shared.domain.practices.usecase.GetPracticeScriptUseCase
import com.example.amulet.shared.domain.practices.usecase.GetCategoriesStreamUseCase
import com.example.amulet.shared.domain.practices.usecase.GetFavoritesStreamUseCase
import com.example.amulet.shared.domain.practices.usecase.GetPracticeByIdUseCase
import com.example.amulet.shared.domain.practices.usecase.GetPracticesStreamUseCase
import com.example.amulet.shared.domain.practices.usecase.GetRecommendationsStreamUseCase
import com.example.amulet.shared.domain.practices.usecase.GetSessionsHistoryStreamUseCase
import com.example.amulet.shared.domain.practices.usecase.GetScheduledSessionsStreamUseCase
import com.example.amulet.shared.domain.practices.usecase.GetScheduledSessionsForDateRangeUseCase
import com.example.amulet.shared.domain.practices.usecase.GetScheduleByPracticeIdUseCase
import com.example.amulet.shared.domain.practices.usecase.GetUserPreferencesStreamUseCase
import com.example.amulet.shared.domain.practices.usecase.RefreshPracticesUseCase
import com.example.amulet.shared.domain.practices.usecase.RefreshPracticesCatalogUseCase
import com.example.amulet.shared.domain.practices.usecase.SearchPracticesUseCase
import com.example.amulet.shared.domain.practices.usecase.SetFavoritePracticeUseCase
import com.example.amulet.shared.domain.practices.usecase.StartPracticeUseCase
import com.example.amulet.shared.domain.practices.usecase.StopSessionUseCase
import com.example.amulet.shared.domain.practices.usecase.UpdateUserPreferencesUseCase
import com.example.amulet.shared.domain.practices.usecase.UpsertPracticeScheduleUseCase
import com.example.amulet.shared.domain.practices.usecase.DeletePracticeScheduleUseCase
import com.example.amulet.shared.domain.practices.usecase.DeleteSchedulesForCourseUseCase
import com.example.amulet.shared.domain.practices.usecase.SkipScheduledSessionUseCase
import com.example.amulet.shared.domain.practices.usecase.LogMoodSelectionUseCase
import com.example.amulet.shared.domain.privacy.PrivacyRepository
import com.example.amulet.shared.domain.rules.RulesRepository
import com.example.amulet.shared.domain.user.repository.UserRepository
import com.example.amulet.shared.domain.user.usecase.ObserveCurrentUserUseCase
import com.example.amulet.shared.domain.courses.CoursesRepository
import com.example.amulet.shared.domain.courses.usecase.CompleteCourseItemUseCase
import com.example.amulet.shared.domain.courses.usecase.ContinueCourseUseCase
import com.example.amulet.shared.domain.courses.usecase.GetCourseByIdUseCase
import com.example.amulet.shared.domain.courses.usecase.GetCourseItemsStreamUseCase
import com.example.amulet.shared.domain.courses.usecase.GetCourseProgressStreamUseCase
import com.example.amulet.shared.domain.courses.usecase.GetCoursesStreamUseCase
import com.example.amulet.shared.domain.courses.usecase.GetAllCoursesProgressStreamUseCase
import com.example.amulet.shared.domain.courses.usecase.GetCoursesByPracticeIdUseCase
import com.example.amulet.shared.domain.courses.usecase.RefreshCoursesUseCase
import com.example.amulet.shared.domain.courses.usecase.RefreshCoursesCatalogUseCase
import com.example.amulet.shared.domain.courses.usecase.ResetCourseProgressUseCase
import com.example.amulet.shared.domain.courses.usecase.StartCourseUseCase
import com.example.amulet.shared.domain.courses.usecase.GetCourseModulesStreamUseCase
import com.example.amulet.shared.domain.courses.usecase.SearchCoursesUseCase
import com.example.amulet.shared.domain.courses.usecase.CheckItemUnlockUseCase
import com.example.amulet.shared.domain.courses.usecase.GetUnlockedItemsUseCase
import com.example.amulet.shared.domain.courses.usecase.GetNextCourseItemUseCase
import com.example.amulet.shared.domain.courses.usecase.CreateScheduleForCourseUseCase
import com.example.amulet.shared.domain.courses.usecase.EnrollCourseUseCase
import com.example.amulet.shared.domain.initialization.usecase.SeedLocalDataUseCase
import com.example.amulet_android_app.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.Koin
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module

@Module
@InstallIn(SingletonComponent::class)
object KoinBridgeModule {

    @Provides
    @Singleton
    fun provideKoin(
        application: Application,
        userSessionProvider: UserSessionProvider,
        userSessionUpdater: UserSessionUpdater,
        authRepository: AuthRepository,
        userRepository: UserRepository,
        devicesRepository: DevicesRepository,
        otaRepository: OtaRepository,
        hugsRepository: HugsRepository,
        patternsRepository: PatternsRepository,
        practicesRepository: PracticesRepository,
        moodRepository: MoodRepository,
        coursesRepository: CoursesRepository,
        privacyRepository: PrivacyRepository,
        rulesRepository: RulesRepository
    ): Koin =
        GlobalContext.getOrNull() ?: startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.NONE)
            androidContext(application)
            val bridgeModule = module {
                single<UserSessionProvider> { userSessionProvider }
                single<UserSessionUpdater> { userSessionUpdater }
                single<AuthRepository> { authRepository }
                single<UserRepository> { userRepository }
                single<DevicesRepository> { devicesRepository }
                single<OtaRepository> { otaRepository }
                single<HugsRepository> { hugsRepository }
                single<PatternsRepository> { patternsRepository }
                single<PracticesRepository> { practicesRepository }
                single<MoodRepository> { moodRepository }
                single<CoursesRepository> { coursesRepository }
                single<PrivacyRepository> { privacyRepository }
                single<RulesRepository> { rulesRepository }
                
                // Initialization UseCase
                single<SeedLocalDataUseCase> {
                    SeedLocalDataUseCase(
                        practicesRepository = practicesRepository,
                        patternsRepository = patternsRepository,
                        coursesRepository = coursesRepository
                    )
                }
            }
            modules(sharedKoinModules() + bridgeModule)
        }.koin

    @Provides
    fun provideSendHugUseCase(koin: Koin): SendHugUseCase = koin.get()

    @Provides
    fun provideSignInUseCase(koin: Koin): SignInUseCase = koin.get()

    @Provides
    fun provideSignInWithGoogleUseCase(koin: Koin): SignInWithGoogleUseCase = koin.get()

    @Provides
    fun provideSignOutUseCase(koin: Koin): SignOutUseCase = koin.get()

    @Provides
    fun provideSignUpUseCase(koin: Koin): SignUpUseCase = koin.get()

    @Provides
    fun provideEnableGuestModeUseCase(koin: Koin): EnableGuestModeUseCase = koin.get()
    
    // User UseCases
    @Provides
    fun provideObserveCurrentUserUseCase(koin: Koin): ObserveCurrentUserUseCase = koin.get()

    // Devices UseCases
    @Provides
    fun provideObserveDevicesUseCase(koin: Koin): ObserveDevicesUseCase = koin.get()

    @Provides
    fun provideGetDeviceUseCase(koin: Koin): GetDeviceUseCase = koin.get()

    @Provides
    fun provideScanForDevicesUseCase(koin: Koin): ScanForDevicesUseCase = koin.get()

    @Provides
    fun provideAddDeviceUseCase(koin: Koin): AddDeviceUseCase = koin.get()

    @Provides
    fun provideRemoveDeviceUseCase(koin: Koin): RemoveDeviceUseCase = koin.get()

    @Provides
    fun provideConnectToDeviceUseCase(koin: Koin): ConnectToDeviceUseCase = koin.get()

    @Provides
    fun provideDisconnectFromDeviceUseCase(koin: Koin): DisconnectFromDeviceUseCase = koin.get()

    @Provides
    fun provideObserveConnectionStateUseCase(koin: Koin): ObserveConnectionStateUseCase = koin.get()

    @Provides
    fun provideObserveConnectedDeviceStatusUseCase(koin: Koin): ObserveConnectedDeviceStatusUseCase = koin.get()

    @Provides
    fun provideObserveDeviceSessionStatusUseCase(koin: Koin): ObserveDeviceSessionStatusUseCase = koin.get()

    @Provides
    fun provideUpdateDeviceSettingsUseCase(koin: Koin): UpdateDeviceSettingsUseCase = koin.get()

    // OTA UseCases
    @Provides
    fun provideCheckFirmwareUpdateUseCase(koin: Koin): CheckFirmwareUpdateUseCase = koin.get()

    @Provides
    fun provideStartBleOtaUpdateUseCase(koin: Koin): StartBleOtaUpdateUseCase = koin.get()

    @Provides
    fun provideStartWifiOtaUpdateUseCase(koin: Koin): StartWifiOtaUpdateUseCase = koin.get()

    @Provides
    fun provideCancelOtaUpdateUseCase(koin: Koin): CancelOtaUpdateUseCase = koin.get()
    
    // Patterns Compiler
    @Provides
    fun providePatternCompiler(koin: Koin): PatternCompiler = koin.get()
    
    // Patterns UseCases
    @Provides
    fun providePatternValidator(koin: Koin): PatternValidator = koin.get()
    
    @Provides
    fun provideCreatePatternUseCase(koin: Koin): CreatePatternUseCase = koin.get()
    
    @Provides
    fun provideUpdatePatternUseCase(koin: Koin): UpdatePatternUseCase = koin.get()
    
    @Provides
    fun provideDeletePatternUseCase(koin: Koin): DeletePatternUseCase = koin.get()
    
    @Provides
    fun provideGetPatternsStreamUseCase(koin: Koin): GetPatternsStreamUseCase = koin.get()
    
    @Provides
    fun provideGetPresetsUseCase(koin: Koin): GetPresetsUseCase = koin.get()
    
    @Provides
    fun provideGetPatternByIdUseCase(koin: Koin): GetPatternByIdUseCase = koin.get()
    
    @Provides
    fun provideObserveMyPatternsUseCase(koin: Koin): ObserveMyPatternsUseCase = koin.get()
    
    @Provides
    fun provideSyncPatternsUseCase(koin: Koin): SyncPatternsUseCase = koin.get()
    
    @Provides
    fun providePublishPatternUseCase(koin: Koin): PublishPatternUseCase = koin.get()
    
    @Provides
    fun provideSharePatternUseCase(koin: Koin): SharePatternUseCase = koin.get()
    
    @Provides
    fun provideAddTagToPatternUseCase(koin: Koin): AddTagToPatternUseCase = koin.get()
    
    @Provides
    fun provideRemoveTagFromPatternUseCase(koin: Koin): RemoveTagFromPatternUseCase = koin.get()
    
    @Provides
    fun provideGetAllTagsUseCase(koin: Koin): GetAllTagsUseCase = koin.get()

    @Provides
    fun provideSetPatternTagsUseCase(koin: Koin): SetPatternTagsUseCase = koin.get()

    @Provides
    fun provideDeleteTagsUseCase(koin: Koin): DeleteTagsUseCase = koin.get()

    @Provides
    fun provideCreateTagsUseCase(koin: Koin): CreateTagsUseCase = koin.get()

    @Provides fun providePreviewPatternOnDeviceUseCase(koin: Koin): PreviewPatternOnDeviceUseCase = koin.get()
    @Provides fun provideClearCurrentDevicePatternUseCase(koin: Koin): ClearCurrentDevicePatternUseCase = koin.get()

    // Practices UseCases
    @Provides fun provideGetPracticesStreamUseCase(koin: Koin): GetPracticesStreamUseCase = koin.get()
    @Provides fun provideGetPracticeByIdUseCase(koin: Koin): GetPracticeByIdUseCase = koin.get()
    @Provides fun provideGetCategoriesStreamUseCase(koin: Koin): GetCategoriesStreamUseCase = koin.get()
    @Provides fun provideGetFavoritesStreamUseCase(koin: Koin): GetFavoritesStreamUseCase = koin.get()
    @Provides fun provideSearchPracticesUseCase(koin: Koin): SearchPracticesUseCase = koin.get()
    @Provides fun provideRefreshPracticesUseCase(koin: Koin): RefreshPracticesUseCase = koin.get()
    @Provides fun provideSetFavoritePracticeUseCase(koin: Koin): SetFavoritePracticeUseCase = koin.get()
    @Provides fun provideGetActiveSessionStreamUseCase(koin: Koin): GetActiveSessionStreamUseCase = koin.get()
    @Provides fun provideGetSessionsHistoryStreamUseCase(koin: Koin): GetSessionsHistoryStreamUseCase = koin.get()
    @Provides fun provideGetScheduledSessionsStreamUseCase(koin: Koin): GetScheduledSessionsStreamUseCase = koin.get()
    @Provides fun provideGetScheduledSessionsForDateRangeUseCase(koin: Koin): GetScheduledSessionsForDateRangeUseCase = koin.get()
    @Provides fun provideStartPracticeUseCase(koin: Koin): StartPracticeUseCase = koin.get()
    @Provides fun provideStopSessionUseCase(koin: Koin): StopSessionUseCase = koin.get()
    @Provides fun provideGetUserPreferencesStreamUseCase(koin: Koin): GetUserPreferencesStreamUseCase = koin.get()
    @Provides fun provideUpdateUserPreferencesUseCase(koin: Koin): UpdateUserPreferencesUseCase = koin.get()
    @Provides fun provideGetRecommendationsStreamUseCase(koin: Koin): GetRecommendationsStreamUseCase = koin.get()
    @Provides fun provideRefreshPracticesCatalogUseCase(koin: Koin): RefreshPracticesCatalogUseCase = koin.get()
    @Provides fun provideUpsertPracticeScheduleUseCase(koin: Koin): UpsertPracticeScheduleUseCase = koin.get()
    @Provides fun provideGetScheduleByPracticeIdUseCase(koin: Koin): GetScheduleByPracticeIdUseCase = koin.get()
    @Provides fun provideDeletePracticeScheduleUseCase(koin: Koin): DeletePracticeScheduleUseCase = koin.get()
    @Provides fun provideDeleteSchedulesForCourseUseCase(koin: Koin): DeleteSchedulesForCourseUseCase = koin.get()
    @Provides fun provideSkipScheduledSessionUseCase(koin: Koin): SkipScheduledSessionUseCase = koin.get()
    @Provides fun provideLogMoodSelectionUseCase(koin: Koin): LogMoodSelectionUseCase = koin.get()
    @Provides fun provideCompletePracticeSessionUseCase(koin: Koin): CompletePracticeSessionUseCase = koin.get()
    @Provides fun provideGetPracticeScriptUseCase(koin: Koin): GetPracticeScriptUseCase = koin.get()

    // Practices Manager
    @Provides fun providePracticeSessionManager(koin: Koin): PracticeSessionManager = koin.get()

    // Courses UseCases
    @Provides fun provideGetCoursesStreamUseCase(koin: Koin): GetCoursesStreamUseCase = koin.get()
    @Provides fun provideGetCourseByIdUseCase(koin: Koin): GetCourseByIdUseCase = koin.get()
    @Provides fun provideGetCourseItemsStreamUseCase(koin: Koin): GetCourseItemsStreamUseCase = koin.get()
    @Provides fun provideGetCourseProgressStreamUseCase(koin: Koin): GetCourseProgressStreamUseCase = koin.get()
    @Provides fun provideGetAllCoursesProgressStreamUseCase(koin: Koin): GetAllCoursesProgressStreamUseCase = koin.get()
    @Provides fun provideGetCoursesByPracticeIdUseCase(koin: Koin): GetCoursesByPracticeIdUseCase = koin.get()
    @Provides fun provideRefreshCoursesUseCase(koin: Koin): RefreshCoursesUseCase = koin.get()
    @Provides fun provideRefreshCoursesCatalogUseCase(koin: Koin): RefreshCoursesCatalogUseCase = koin.get()
    @Provides fun provideStartCourseUseCase(koin: Koin): StartCourseUseCase = koin.get()
    @Provides fun provideContinueCourseUseCase(koin: Koin): ContinueCourseUseCase = koin.get()
    @Provides fun provideCompleteCourseItemUseCase(koin: Koin): CompleteCourseItemUseCase = koin.get()
    @Provides fun provideResetCourseProgressUseCase(koin: Koin): ResetCourseProgressUseCase = koin.get()
    @Provides fun provideGetCourseModulesStreamUseCase(koin: Koin): GetCourseModulesStreamUseCase = koin.get()
    @Provides fun provideSearchCoursesUseCase(koin: Koin): SearchCoursesUseCase = koin.get()
    @Provides fun provideCheckItemUnlockUseCase(koin: Koin): CheckItemUnlockUseCase = koin.get()
    @Provides fun provideGetUnlockedItemsUseCase(koin: Koin): GetUnlockedItemsUseCase = koin.get()
    @Provides fun provideCreateScheduleForCourseUseCase(koin: Koin): CreateScheduleForCourseUseCase = koin.get()
    @Provides fun provideEnrollCourseUseCase(koin: Koin): EnrollCourseUseCase = koin.get()
    
    // Initialization UseCase
    @Provides fun provideSeedLocalDataUseCase(koin: Koin): SeedLocalDataUseCase = koin.get()
}
