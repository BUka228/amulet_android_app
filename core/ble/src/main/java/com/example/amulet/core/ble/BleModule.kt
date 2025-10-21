package com.example.amulet.core.ble

import com.example.amulet.core.ble.internal.AmuletBleManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt модуль для предоставления BLE зависимостей.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class BleModule {
    
    @Binds
    @Singleton
    abstract fun bindAmuletBleManager(
        impl: AmuletBleManagerImpl
    ): AmuletBleManager
}
