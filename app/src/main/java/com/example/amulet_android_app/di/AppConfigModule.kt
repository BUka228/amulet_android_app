package com.example.amulet_android_app.di

import com.example.amulet.core.supabase.SupabaseEnvironment
import com.example.amulet.core.turnstile.TurnstileEnvironment
import com.example.amulet_android_app.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppConfigModule {

    @Provides
    @Singleton
    fun provideSupabaseEnvironment(): SupabaseEnvironment =
        SupabaseEnvironment(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            restUrl = BuildConfig.SUPABASE_REST_URL,
            anonKey = BuildConfig.SUPABASE_ANON_KEY
        )

    @Provides
    @Singleton
    fun provideTurnstileEnvironment(): TurnstileEnvironment =
        TurnstileEnvironment(siteKey = BuildConfig.TURNSTILE_SITE_KEY)
}
