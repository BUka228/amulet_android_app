package com.example.amulet.core.database

import android.content.Context
import androidx.room.Room
import com.example.amulet.core.database.dao.DeviceDao
import com.example.amulet.core.database.dao.FirmwareInfoDao
import com.example.amulet.core.database.dao.HugDao
import com.example.amulet.core.database.dao.OutboxActionDao
import com.example.amulet.core.database.dao.PairDao
import com.example.amulet.core.database.dao.PatternDao
import com.example.amulet.core.database.dao.PracticeDao
import com.example.amulet.core.database.dao.PrivacyJobDao
import com.example.amulet.core.database.dao.RemoteKeyDao
import com.example.amulet.core.database.dao.RuleDao
import com.example.amulet.core.database.dao.TelemetryDao
import com.example.amulet.core.database.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "amulet_app.db"

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AmuletDatabase =
        Room.databaseBuilder(context, AmuletDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()

    @Provides
    fun provideUserDao(database: AmuletDatabase): UserDao = database.userDao()

    @Provides
    fun provideDeviceDao(database: AmuletDatabase): DeviceDao = database.deviceDao()

    @Provides
    fun provideHugDao(database: AmuletDatabase): HugDao = database.hugDao()

    @Provides
    fun providePatternDao(database: AmuletDatabase): PatternDao = database.patternDao()

    @Provides
    fun providePairDao(database: AmuletDatabase): PairDao = database.pairDao()

    @Provides
    fun providePracticeDao(database: AmuletDatabase): PracticeDao = database.practiceDao()

    @Provides
    fun provideRuleDao(database: AmuletDatabase): RuleDao = database.ruleDao()

    @Provides
    fun provideTelemetryDao(database: AmuletDatabase): TelemetryDao = database.telemetryDao()

    @Provides
    fun providePrivacyJobDao(database: AmuletDatabase): PrivacyJobDao = database.privacyJobDao()

    @Provides
    fun provideFirmwareInfoDao(database: AmuletDatabase): FirmwareInfoDao = database.firmwareInfoDao()

    @Provides
    fun provideOutboxActionDao(database: AmuletDatabase): OutboxActionDao = database.outboxActionDao()

    @Provides
    fun provideRemoteKeyDao(database: AmuletDatabase): RemoteKeyDao = database.remoteKeyDao()
}
