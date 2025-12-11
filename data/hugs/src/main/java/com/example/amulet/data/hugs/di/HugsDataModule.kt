package com.example.amulet.data.hugs.di

import com.example.amulet.core.database.entity.OutboxActionType
import com.example.amulet.core.sync.processing.ActionProcessor
import com.example.amulet.core.sync.processing.ActionProcessorKey
import com.example.amulet.data.hugs.HugDevicePlayActionProcessor
import com.example.amulet.data.hugs.HugSendActionProcessor
import com.example.amulet.data.hugs.PairSettingsUpdateActionProcessor
import com.example.amulet.data.hugs.HugsRepositoryImpl
import com.example.amulet.data.hugs.PairsRepositoryImpl
import com.example.amulet.data.hugs.datasource.local.HugsLocalDataSource
import com.example.amulet.data.hugs.datasource.local.HugsLocalDataSourceImpl
import com.example.amulet.data.hugs.datasource.local.PairsLocalDataSource
import com.example.amulet.data.hugs.datasource.local.PairsLocalDataSourceImpl
import com.example.amulet.data.hugs.datasource.remote.HugsRemoteDataSource
import com.example.amulet.data.hugs.datasource.remote.HugsRemoteDataSourceImpl
import com.example.amulet.data.hugs.datasource.remote.PairsRemoteDataSource
import com.example.amulet.data.hugs.datasource.remote.PairsRemoteDataSourceImpl
import com.example.amulet.shared.domain.hugs.HugsRepository
import com.example.amulet.shared.domain.hugs.PairsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface HugsDataModule {

    @Binds
    @Singleton
    fun bindHugsRepository(impl: HugsRepositoryImpl): HugsRepository

    @Binds
    @Singleton
    fun bindPairsRepository(impl: PairsRepositoryImpl): PairsRepository

    @Binds
    @Singleton
    fun bindHugsLocalDataSource(impl: HugsLocalDataSourceImpl): HugsLocalDataSource

    @Binds
    @Singleton
    fun bindPairsLocalDataSource(impl: PairsLocalDataSourceImpl): PairsLocalDataSource

    @Binds
    @Singleton
    fun bindHugsRemoteDataSource(impl: HugsRemoteDataSourceImpl): HugsRemoteDataSource

    @Binds
    @Singleton
    fun bindPairsRemoteDataSource(impl: PairsRemoteDataSourceImpl): PairsRemoteDataSource

    // Outbox ActionProcessors for hugs

    @Binds
    @IntoMap
    @ActionProcessorKey(OutboxActionType.HUG_DEVICE_PLAY)
    fun bindHugDevicePlayActionProcessor(impl: HugDevicePlayActionProcessor): ActionProcessor

    @Binds
    @IntoMap
    @ActionProcessorKey(OutboxActionType.PAIR_SETTINGS_UPDATE)
    fun bindPairSettingsUpdateActionProcessor(impl: PairSettingsUpdateActionProcessor): ActionProcessor

    @Binds
    @IntoMap
    @ActionProcessorKey(OutboxActionType.HUG_SEND)
    fun bindHugSendActionProcessor(impl: HugSendActionProcessor): ActionProcessor
}
