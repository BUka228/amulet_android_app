package com.example.amulet.data.notifications.di

import com.example.amulet.data.notifications.repository.NotificationsRepositoryImpl
import com.example.amulet.shared.domain.notifications.repository.NotificationsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationsDataModule {

    @Binds
    abstract fun bindNotificationsRepository(impl: NotificationsRepositoryImpl): NotificationsRepository
}
