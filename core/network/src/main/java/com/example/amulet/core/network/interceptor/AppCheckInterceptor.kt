package com.example.amulet.core.network.interceptor

import com.example.amulet.core.network.auth.AppCheckTokenProvider
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AppCheckInterceptor(
    private val appCheckTokenProvider: AppCheckTokenProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = runBlocking { appCheckTokenProvider.getAppCheckToken() }
        if (token.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }
        val requestWithHeader = originalRequest.newBuilder()
            .header(APP_CHECK_HEADER, token)
            .build()
        return chain.proceed(requestWithHeader)
    }

    private companion object {
        const val APP_CHECK_HEADER = "X-Firebase-AppCheck"
    }
}
