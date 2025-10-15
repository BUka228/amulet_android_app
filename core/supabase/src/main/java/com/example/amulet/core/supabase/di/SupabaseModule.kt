package com.example.amulet.core.supabase.di

import com.example.amulet.core.supabase.SupabaseEnvironment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.storage.storage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(environment: SupabaseEnvironment): SupabaseClient =
        createSupabaseClient(environment.supabaseUrl, environment.anonKey) {
            install(Auth) {
                autoLoadFromStorage = false
                autoSaveToStorage = false
                alwaysAutoRefresh = false
                sessionManager = MemorySessionManager()
                enableLifecycleCallbacks = false
            }
        }

    @Provides
    fun provideGoTrue(client: SupabaseClient) = client.auth

    @Provides
    fun provideFunctions(client: SupabaseClient) = client.functions

    @Provides
    fun provideStorage(client: SupabaseClient) = client.storage
}
