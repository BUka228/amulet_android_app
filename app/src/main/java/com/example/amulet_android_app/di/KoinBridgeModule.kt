package com.example.amulet_android_app.di

import android.app.Application
import com.example.amulet.data.auth.authDataModule
import com.example.amulet.data.devices.devicesDataModule
import com.example.amulet.data.hugs.hugsDataModule
import com.example.amulet.data.patterns.patternsDataModule
import com.example.amulet.data.practices.practicesDataModule
import com.example.amulet.data.privacy.privacyDataModule
import com.example.amulet.data.rules.rulesDataModule
import com.example.amulet.data.user.userDataModule
import com.example.amulet.shared.core.auth.UserSessionProvider
import com.example.amulet.shared.core.auth.UserSessionUpdater
import com.example.amulet.shared.di.sharedKoinModules
import com.example.amulet.shared.domain.auth.repository.AuthRepository
import com.example.amulet.shared.domain.devices.DevicesRepository
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
import org.koin.core.Koin
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

@Module
@InstallIn(SingletonComponent::class)
object KoinBridgeModule {

    @Provides
    @Singleton
    fun provideKoin(
        application: Application,
        userSessionProvider: UserSessionProvider,
        userSessionUpdater: UserSessionUpdater
    ): Koin =
        GlobalContext.getOrNull() ?: startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.NONE)
            androidContext(application)
            val dataModules = listOf(
                authDataModule,
                devicesDataModule,
                hugsDataModule,
                patternsDataModule,
                practicesDataModule,
                privacyDataModule,
                rulesDataModule,
                userDataModule
            )
            val bridgeModule = module {
                single<UserSessionProvider> { userSessionProvider }
                single<UserSessionUpdater> { userSessionUpdater }
            }
            modules(sharedKoinModules() + dataModules + bridgeModule)
        }.koin

    @Provides
    fun provideSendHugUseCase(koin: Koin): SendHugUseCase = koin.get()

    @Provides
    fun provideHugsRepository(koin: Koin): HugsRepository = koin.get()

    @Provides
    fun provideAuthRepository(koin: Koin): AuthRepository = koin.get()

    @Provides
    fun provideDevicesRepository(koin: Koin): DevicesRepository = koin.get()

    @Provides
    fun providePatternsRepository(koin: Koin): PatternsRepository = koin.get()

    @Provides
    fun providePracticesRepository(koin: Koin): PracticesRepository = koin.get()

    @Provides
    fun providePrivacyRepository(koin: Koin): PrivacyRepository = koin.get()

    @Provides
    fun provideRulesRepository(koin: Koin): RulesRepository = koin.get()

    @Provides
    fun provideUserRepository(koin: Koin): UserRepository = koin.get()
}
