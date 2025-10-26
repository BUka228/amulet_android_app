package com.example.amulet.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Удаляем колонку serialNumber из таблицы devices
            // SQLite не поддерживает DROP COLUMN, поэтому пересоздаем таблицу
            db.execSQL("""
                CREATE TABLE devices_new (
                    id TEXT PRIMARY KEY NOT NULL,
                    name TEXT NOT NULL,
                    bleAddress TEXT NOT NULL,
                    hardwareVersion INTEGER NOT NULL,
                    isConnected INTEGER NOT NULL DEFAULT 0,
                    lastSyncedAt INTEGER,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
            """.trimIndent())
            
            db.execSQL("""
                INSERT INTO devices_new (id, name, bleAddress, hardwareVersion, isConnected, lastSyncedAt, createdAt, updatedAt)
                SELECT id, name, bleAddress, hardwareVersion, isConnected, lastSyncedAt, createdAt, updatedAt
                FROM devices
            """.trimIndent())
            
            db.execSQL("DROP TABLE devices")
            db.execSQL("ALTER TABLE devices_new RENAME TO devices")
            
            // Пересоздаем индекс
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_devices_bleAddress ON devices(bleAddress)")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AmuletDatabase =
        Room.databaseBuilder(context, AmuletDatabase::class.java, DATABASE_NAME)
            .addMigrations(MIGRATION_1_2)
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
