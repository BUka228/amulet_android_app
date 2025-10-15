package com.example.amulet.core.turnstile

data class TurnstileEnvironment(
    val siteKey: String,
    val verifyEndpoint: String? = null
)
