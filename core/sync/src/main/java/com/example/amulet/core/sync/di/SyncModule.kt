package com.example.amulet.core.sync.di

import com.example.amulet.core.database.AmuletDatabase
import com.example.amulet.core.database.dao.OutboxActionDao
import com.example.amulet.core.sync.internal.ActionErrorResolver
import com.example.amulet.core.sync.internal.BackoffPolicy
import com.example.amulet.core.sync.internal.OutboxActionStore
import com.example.amulet.core.sync.internal.OutboxEngine
import com.example.amulet.core.sync.internal.OutboxSyncConfig
import com.example.amulet.core.sync.internal.OutboxSyncWatchdog
import com.example.amulet.core.sync.internal.RoomOutboxActionStore
import com.example.amulet.core.sync.internal.SystemTimeProvider
import com.example.amulet.core.sync.internal.TimeProvider
import com.example.amulet.core.sync.processing.ActionProcessorFactory
import com.example.amulet.core.sync.processing.DefaultActionProcessorFactory
import com.example.amulet.core.sync.scheduler.OutboxScheduler
import com.example.amulet.core.sync.scheduler.WorkManagerOutboxScheduler
import com.example.amulet.core.sync.worker.OutboxWorkerRunner
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    abstract fun bindOutboxScheduler(impl: WorkManagerOutboxScheduler): OutboxScheduler

    @Binds
    abstract fun bindActionProcessorFactory(impl: DefaultActionProcessorFactory): ActionProcessorFactory

    companion object {
        private const val DEFAULT_MAX_ACTIONS_PER_SYNC = Int.MAX_VALUE
        private const val DEFAULT_IN_FLIGHT_TIMEOUT_MINUTES = 5L
        private const val DEFAULT_BASE_BACKOFF_MILLIS = 5_000L
        private const val DEFAULT_MAX_BACKOFF_MILLIS = 5 * 60_000L

        @Provides
        @Singleton
        fun provideTimeProvider(): TimeProvider = SystemTimeProvider

        @Provides
        @Singleton
        fun provideSyncConfig(): OutboxSyncConfig = OutboxSyncConfig(
            maxActionsPerSync = DEFAULT_MAX_ACTIONS_PER_SYNC,
            inFlightTimeoutMillis = DEFAULT_IN_FLIGHT_TIMEOUT_MINUTES * 60_000L,
            baseBackoffMillis = DEFAULT_BASE_BACKOFF_MILLIS,
            maxBackoffMillis = DEFAULT_MAX_BACKOFF_MILLIS
        )

        @Provides
        @Singleton
        fun provideBackoffPolicy(config: OutboxSyncConfig): BackoffPolicy =
            BackoffPolicy(
                baseDelayMillis = config.baseBackoffMillis,
                maxDelayMillis = config.maxBackoffMillis
            )

        @Provides
        @Singleton
        fun provideOutboxActionStore(
            database: AmuletDatabase,
            outboxActionDao: OutboxActionDao
        ): OutboxActionStore = RoomOutboxActionStore(database, outboxActionDao)

        @Provides
        @Singleton
        fun provideOutboxEngine(
            store: OutboxActionStore,
            processorFactory: ActionProcessorFactory,
            errorResolver: ActionErrorResolver,
            timeProvider: TimeProvider,
            config: OutboxSyncConfig
        ): OutboxEngine = OutboxEngine(
            store = store,
            processorFactory = processorFactory,
            errorResolver = errorResolver,
            timeProvider = timeProvider,
            config = config
        )

        @Provides
        @Singleton
        fun provideOutboxSyncWatchdog(
            store: OutboxActionStore,
            timeProvider: TimeProvider,
            config: OutboxSyncConfig
        ): OutboxSyncWatchdog = OutboxSyncWatchdog(
            store = store,
            timeProvider = timeProvider,
            config = config
        )

        @Provides
        @Singleton
        fun provideOutboxWorkerRunner(
            engine: OutboxEngine,
            watchdog: OutboxSyncWatchdog,
            timeProvider: TimeProvider
        ): OutboxWorkerRunner = OutboxWorkerRunner(
            engine = engine,
            watchdog = watchdog,
            timeProvider = timeProvider
        )
    }
}
