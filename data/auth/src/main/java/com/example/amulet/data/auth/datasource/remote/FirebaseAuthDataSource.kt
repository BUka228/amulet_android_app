package com.example.amulet.data.auth.datasource.remote

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.auth.model.UserCredentials
import com.example.amulet.shared.domain.user.model.UserId
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class FirebaseAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRemoteDataSource {

    override suspend fun signIn(credentials: UserCredentials): AppResult<UserId> = runCatching {
        val result = firebaseAuth.signInWithEmailAndPassword(credentials.email, credentials.password).await()
        result.user?.uid?.let(::UserId) ?: throw IllegalStateException("Missing Firebase user id")
    }.fold(
        onSuccess = { uid -> Ok(uid) },
        onFailure = { throwable -> Err(throwable.toAppError()) }
    )

    override suspend fun signInWithGoogle(idToken: String): AppResult<UserId> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = firebaseAuth.signInWithCredential(credential).await()
        result.user?.uid?.let(::UserId) ?: throw IllegalStateException("Missing Firebase user id")
    }.fold(
        onSuccess = { uid -> Ok(uid) },
        onFailure = { throwable -> Err(throwable.toAppError()) }
    )

    override suspend fun signOut(): AppResult<Unit> = runCatching {
        firebaseAuth.signOut()
    }.fold(
        onSuccess = { Ok(Unit) },
        onFailure = { throwable -> Err(throwable.toAppError()) }
    )

    private fun Throwable.toAppError(): AppError = when (this) {
        is FirebaseAuthInvalidCredentialsException -> AppError.Unauthorized
        is FirebaseAuthInvalidUserException -> AppError.Unauthorized
        is FirebaseNetworkException -> AppError.Network
        else -> AppError.Unknown
    }
}
