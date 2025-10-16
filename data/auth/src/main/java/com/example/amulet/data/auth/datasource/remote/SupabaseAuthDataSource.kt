package com.example.amulet.data.auth.datasource.remote

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.logging.Logger
import com.example.amulet.shared.domain.auth.model.UserCredentials
import com.example.amulet.shared.domain.user.model.UserId
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.exceptions.RestException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class SupabaseAuthDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) : AuthRemoteDataSource {

    private val auth get() = supabaseClient.auth

    override suspend fun signUp(credentials: UserCredentials): AppResult<UserId> =
        runAuth("signUp") {
            auth.signUpWith(Email) {
                this.email = credentials.email
                this.password = credentials.password
            }
        }

    override suspend fun signIn(credentials: UserCredentials): AppResult<UserId> =
        runAuth("signIn") {
            auth.signInWith(Email) {
                this.email = credentials.email
                this.password = credentials.password
            }
        }

    override suspend fun signInWithGoogle(idToken: String): AppResult<UserId> =
        runAuth("signInWithGoogle") {
            auth.signInWith(IDToken) {
                this.idToken = idToken
                this.provider = Google
            }
        }

    override suspend fun signOut(): AppResult<Unit> = runCatching {
        Logger.d("Supabase signOut requested", TAG)
        withContext(Dispatchers.IO) {
            auth.signOut()
        }
    }.fold(
        onSuccess = {
            Logger.i("Supabase signOut success", TAG)
            Ok(Unit)
        },
        onFailure = { throwable ->
            Logger.w("Supabase signOut failed", throwable, TAG)
            Err(throwable.toAppError())
        }
    )

    private suspend fun runAuth(
        action: String,
        block: suspend () -> Unit
    ): AppResult<UserId> = runCatching {
        Logger.d("$action requested", TAG)
        withContext(Dispatchers.IO) { block() }
    }.fold(
        onSuccess = {
            val session = auth.currentSessionOrNull()
                ?: error("Missing Supabase session")
            val userId = session.user?.id ?: error("Missing Supabase user")
            Logger.i("$action success user=$userId", TAG)
            Ok(UserId(userId))
        },
        onFailure = { throwable ->
            Logger.w("$action failed", throwable, TAG)
            Err(throwable.toAppError())
        }
    )


    private fun Throwable.toAppError(): AppError = when (this) {
        is RestException -> when (statusCode) {
            400, 422 -> AppError.Validation(
                message?.let { mapOf("message" to it) } ?: emptyMap()
            )
            401 -> AppError.Unauthorized
            403 -> AppError.Forbidden
            else -> AppError.Unknown
        }
        else -> AppError.Unknown
    }

    private companion object {
        private const val TAG = "SupabaseAuthDataSource"
    }
}
