package com.example.amulet.data.privacy.di

import com.example.amulet.data.privacy.PrivacyRepositoryImpl
import com.example.amulet.shared.domain.privacy.PrivacyRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface PrivacyDataModule {

    @Binds
    @Singleton
    fun bindPrivacyRepository(impl: PrivacyRepositoryImpl): PrivacyRepository
}
