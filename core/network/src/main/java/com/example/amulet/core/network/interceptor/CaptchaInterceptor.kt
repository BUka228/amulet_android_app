package com.example.amulet.core.network.interceptor

import com.example.amulet.core.turnstile.TurnstileTokenStore
import okhttp3.Interceptor
import okhttp3.Response

class CaptchaInterceptor(
    private val tokenStore: TurnstileTokenStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenStore.consumeToken()
        val original = chain.request()
        if (token.isNullOrBlank()) {
            return chain.proceed(original)
        }
        val request = original.newBuilder()
            .header(CAPTCHA_HEADER, token)
            .build()
        return chain.proceed(request)
    }

    private companion object {
        private const val CAPTCHA_HEADER = "X-Captcha-Token"
    }
}
