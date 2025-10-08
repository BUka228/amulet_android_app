package com.example.amulet.core.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.example.amulet.core.auth.datastore.UserSessionPreferencesSerializer
import com.example.amulet.core.auth.session.UserSessionManager
import com.example.amulet.core.auth.session.impl.UserSessionManagerImpl
import com.example.amulet.core.auth.session.proto.UserSessionPreferences
import com.example.amulet.core.auth.token.FirebaseAppCheckTokenProvider
import com.example.amulet.core.auth.token.FirebaseIdTokenProvider
import com.example.amulet.core.network.auth.AppCheckTokenProvider
import com.example.amulet.core.network.auth.IdTokenProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.appcheck.FirebaseAppCheck
import com.example.amulet.shared.core.auth.UserSessionProvider
import com.example.amulet.shared.core.auth.UserSessionUpdater
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    abstract fun bindUserSessionManager(impl: UserSessionManagerImpl): UserSessionManager

    @Binds
    abstract fun bindIdTokenProvider(impl: FirebaseIdTokenProvider): IdTokenProvider

    @Binds
    abstract fun bindAppCheckTokenProvider(impl: FirebaseAppCheckTokenProvider): AppCheckTokenProvider

    companion object {
        private const val STORE_FILE = "user_session.pb"

        @Provides
        @Singleton
        fun provideUserSessionDataStore(
            @ApplicationContext context: Context
        ): DataStore<UserSessionPreferences> = DataStoreFactory.create(
            serializer = UserSessionPreferencesSerializer,
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        ) {
            context.dataStoreFile(STORE_FILE)
        }

        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

        @Provides
        @Singleton
        fun provideFirebaseAppCheck(): FirebaseAppCheck = FirebaseAppCheck.getInstance()

        @Provides
        fun provideUserSessionProvider(manager: UserSessionManager): UserSessionProvider = manager

        @Provides
        fun provideUserSessionUpdater(manager: UserSessionManager): UserSessionUpdater = manager
    }
}
