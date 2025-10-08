package com.example.amulet.core.network.interceptor

import com.example.amulet.core.network.auth.IdTokenProvider
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val idTokenProvider: IdTokenProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = runBlocking { idTokenProvider.getIdToken() }
        if (token.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }
        val authenticatedRequest = originalRequest.newBuilder()
            .header(AUTHORIZATION_HEADER, "Bearer $token")
            .build()
        return chain.proceed(authenticatedRequest)
    }

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
    }
}
