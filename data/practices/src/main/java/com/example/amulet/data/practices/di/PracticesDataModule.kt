package com.example.amulet.data.practices.di

import com.example.amulet.data.practices.PracticesRepositoryImpl
import com.example.amulet.data.practices.MoodRepositoryImpl
import com.example.amulet.data.practices.datasource.LocalPracticesDataSource
import com.example.amulet.data.practices.datasource.LocalPracticesDataSourceImpl
import com.example.amulet.data.practices.datasource.RemotePracticesDataSource
import com.example.amulet.data.practices.datasource.RemotePracticesDataSourceStub
import com.example.amulet.shared.domain.practices.PracticesRepository
import com.example.amulet.shared.domain.practices.MoodRepository
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

    @Binds
    @Singleton
    fun bindLocalPracticesDataSource(impl: LocalPracticesDataSourceImpl): LocalPracticesDataSource

    @Binds
    @Singleton
    fun bindRemotePracticesDataSource(impl: RemotePracticesDataSourceStub): RemotePracticesDataSource

    @Binds
    @Singleton
    fun bindMoodRepository(impl: MoodRepositoryImpl): MoodRepository
}
