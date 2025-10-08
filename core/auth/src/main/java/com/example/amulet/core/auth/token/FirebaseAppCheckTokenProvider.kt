package com.example.amulet.core.auth.token

import com.example.amulet.core.network.auth.AppCheckTokenProvider
import com.example.amulet.shared.core.logging.Logger
import com.google.firebase.appcheck.FirebaseAppCheck
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Singleton
class FirebaseAppCheckTokenProvider @Inject constructor(
    private val firebaseAppCheck: FirebaseAppCheck
) : AppCheckTokenProvider {

    override suspend fun getAppCheckToken(): String? = suspendCancellableCoroutine { continuation ->
        firebaseAppCheck.getToken(false)
            .addOnCompleteListener { task ->
                if (!continuation.isActive) {
                    return@addOnCompleteListener
                }

                when {
                    task.isSuccessful -> {
                        val token = task.result?.token?.takeUnless { it.isBlank() }
                        continuation.resume(token)
                    }

                    task.isCanceled -> {
                        continuation.cancel(
                            task.exception ?: CancellationException("Firebase App Check token request cancelled")
                        )
                    }

                    else -> {
                        Logger.w("Failed to fetch Firebase App Check token", task.exception, TAG)
                        continuation.resume(null)
                    }
                }
            }
    }

    private companion object {
        private const val TAG = "FirebaseAppCheckTokenProvider"
    }
}
