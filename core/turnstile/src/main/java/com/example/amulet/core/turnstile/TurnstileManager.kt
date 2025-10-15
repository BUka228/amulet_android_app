package com.example.amulet.core.turnstile

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.StateFlow

@Singleton
class TurnstileManager @Inject constructor(
    private val environment: TurnstileEnvironment,
    private val tokenStore: TurnstileTokenStore
) {

    val siteKey: String get() = environment.siteKey

    val verifyEndpoint: String? get() = environment.verifyEndpoint

    fun updateToken(token: String?) {
        tokenStore.updateToken(token)
    }

    fun consumeToken(): String? = tokenStore.consumeToken()

    fun tokenFlow(): StateFlow<String?> = tokenStore.tokenFlow
}
