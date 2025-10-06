package com.example.amulet.data.practices.di

import com.example.amulet.data.practices.PracticesRepositoryImpl
import com.example.amulet.shared.domain.practices.PracticesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface PracticesDataModule {

    @Binds
    @Singleton
    fun bindPracticesRepository(impl: PracticesRepositoryImpl): PracticesRepository
}
