package com.example.amulet.core.turnstile

import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class TurnstileTokenStore @Inject constructor() {

    private val tokenRef = AtomicReference<String?>(null)
    private val _tokenFlow = MutableStateFlow<String?>(null)

    val tokenFlow: StateFlow<String?> = _tokenFlow.asStateFlow()

    fun updateToken(token: String?) {
        tokenRef.set(token)
        _tokenFlow.value = token
    }

    fun consumeToken(): String? {
        val token = tokenRef.getAndSet(null)
        if (token != null) {
            _tokenFlow.value = null
        }
        return token
    }

    fun peekToken(): String? = tokenRef.get()
}
