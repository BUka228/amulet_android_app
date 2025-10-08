package com.example.amulet.core.auth.token

import com.example.amulet.core.network.auth.IdTokenProvider
import com.example.amulet.shared.core.logging.Logger
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Singleton
class FirebaseIdTokenProvider @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : IdTokenProvider {

    override suspend fun getIdToken(): String? = suspendCancellableCoroutine { continuation ->
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        currentUser.getIdToken(false)
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
                            task.exception ?: CancellationException("Firebase ID token request cancelled")
                        )
                    }

                    else -> {
                        Logger.w("Failed to fetch Firebase ID token", task.exception, TAG)
                        continuation.resume(null)
                    }
                }
            }
    }

    private companion object {
        private const val TAG = "FirebaseIdTokenProvider"
    }
}
