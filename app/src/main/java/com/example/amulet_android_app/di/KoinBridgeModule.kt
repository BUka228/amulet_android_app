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
import com.example.amulet.shared.domain.hugs.HugsRepository
import com.example.amulet.shared.domain.hugs.SendHugUseCase
import com.example.amulet.shared.domain.patterns.PatternsRepository
import com.example.amulet.shared.domain.practices.PracticesRepository
import com.example.amulet.shared.domain.privacy.PrivacyRepository
import com.example.amulet.shared.domain.rules.RulesRepository
import com.example.amulet.shared.domain.user.repository.UserRepository
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
        hugsRepository: HugsRepository,
        patternsRepository: PatternsRepository,
        practicesRepository: PracticesRepository,
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
                single<HugsRepository> { hugsRepository }
                single<PatternsRepository> { patternsRepository }
                single<PracticesRepository> { practicesRepository }
                single<PrivacyRepository> { privacyRepository }
                single<RulesRepository> { rulesRepository }
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
}
