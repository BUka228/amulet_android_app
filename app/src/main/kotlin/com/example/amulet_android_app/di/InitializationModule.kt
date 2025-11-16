package com.example.amulet_android_app.di

import com.example.amulet.shared.domain.initialization.DataInitializer
import com.example.amulet_android_app.initialization.DataInitializerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class InitializationModule {

    @Binds
    @Singleton
    abstract fun bindDataInitializer(
        dataInitializerImpl: DataInitializerImpl
    ): DataInitializer
}
