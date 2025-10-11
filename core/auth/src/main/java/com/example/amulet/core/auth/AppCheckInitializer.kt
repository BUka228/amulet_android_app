package com.example.amulet.core.auth

import android.content.Context
import com.example.amulet.shared.core.logging.Logger
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppCheckInitializer @Inject constructor() {

    fun initialize(context: Context, isDebug: Boolean) {
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        
        when {
            isDebug -> {
                Logger.i("Initializing App Check with Debug provider", TAG)
                firebaseAppCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
            }
            else -> {
                Logger.i("Initializing App Check with Play Integrity provider", TAG)
                firebaseAppCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                )
            }
        }
        
        Logger.i("App Check initialized successfully", TAG)
    }

    private companion object {
        private const val TAG = "AppCheckInitializer"
    }
}

