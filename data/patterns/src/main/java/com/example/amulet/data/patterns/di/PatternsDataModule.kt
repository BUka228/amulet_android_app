package com.example.amulet.data.patterns.di

import com.example.amulet.core.database.entity.OutboxActionType
import com.example.amulet.core.sync.processing.ActionProcessor
import com.example.amulet.core.sync.processing.ActionProcessorKey
import com.example.amulet.data.patterns.PatternCrudActionProcessor
import com.example.amulet.data.patterns.PatternMarkersActionProcessor
import com.example.amulet.data.patterns.PatternSegmentsActionProcessor
import com.example.amulet.data.patterns.PatternsRepositoryImpl
import com.example.amulet.data.patterns.datasource.LocalPatternDataSource
import com.example.amulet.data.patterns.datasource.LocalPatternDataSourceImpl
import com.example.amulet.data.patterns.datasource.RemotePatternDataSource
import com.example.amulet.data.patterns.datasource.RemotePatternDataSourceImpl
import com.example.amulet.shared.domain.patterns.PatternsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface PatternsDataModule {

    @Binds
    @Singleton
    fun bindPatternsRepository(impl: PatternsRepositoryImpl): PatternsRepository
    
    @Binds
    @Singleton
    fun bindLocalPatternDataSource(impl: LocalPatternDataSourceImpl): LocalPatternDataSource
    
    @Binds
    @Singleton
    fun bindRemotePatternDataSource(impl: RemotePatternDataSourceImpl): RemotePatternDataSource

    @Binds
    @IntoMap
    @ActionProcessorKey(OutboxActionType.PATTERN_CREATE)
    fun bindPatternCreateActionProcessor(impl: PatternCrudActionProcessor): ActionProcessor

    @Binds
    @IntoMap
    @ActionProcessorKey(OutboxActionType.PATTERN_UPDATE)
    fun bindPatternUpdateActionProcessor(impl: PatternCrudActionProcessor): ActionProcessor

    @Binds
    @IntoMap
    @ActionProcessorKey(OutboxActionType.PATTERN_DELETE)
    fun bindPatternDeleteActionProcessor(impl: PatternCrudActionProcessor): ActionProcessor

    @Binds
    @IntoMap
    @ActionProcessorKey(OutboxActionType.PATTERN_SHARE)
    fun bindPatternShareActionProcessor(impl: PatternCrudActionProcessor): ActionProcessor

    @Binds
    @IntoMap
    @ActionProcessorKey(OutboxActionType.PATTERN_SEGMENTS_UPDATE)
    fun bindPatternSegmentsActionProcessor(impl: PatternSegmentsActionProcessor): ActionProcessor

    @Binds
    @IntoMap
    @ActionProcessorKey(OutboxActionType.PATTERN_MARKERS_UPDATE)
    fun bindPatternMarkersActionProcessor(impl: PatternMarkersActionProcessor): ActionProcessor
}
