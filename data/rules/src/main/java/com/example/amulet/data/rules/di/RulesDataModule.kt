package com.example.amulet.data.rules.di

import com.example.amulet.data.rules.RulesRepositoryImpl
import com.example.amulet.shared.domain.rules.RulesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RulesDataModule {

    @Binds
    @Singleton
    fun bindRulesRepository(impl: RulesRepositoryImpl): RulesRepository
}
