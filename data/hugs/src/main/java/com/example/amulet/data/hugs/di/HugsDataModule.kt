package com.example.amulet.data.hugs.di

import com.example.amulet.data.hugs.HugsRepositoryImpl
import com.example.amulet.shared.domain.hugs.HugsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface HugsDataModule {

    @Binds
    @Singleton
    fun bindHugsRepository(impl: HugsRepositoryImpl): HugsRepository
}
