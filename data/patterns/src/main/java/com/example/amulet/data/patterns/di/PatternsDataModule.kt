package com.example.amulet.data.patterns.di

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
}
