package com.example.amulet.data.devices.di

import com.example.amulet.data.devices.DevicesRepositoryImpl
import com.example.amulet.shared.domain.devices.DevicesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DevicesDataModule {

    @Binds
    @Singleton
    fun bindDevicesRepository(impl: DevicesRepositoryImpl): DevicesRepository
}
