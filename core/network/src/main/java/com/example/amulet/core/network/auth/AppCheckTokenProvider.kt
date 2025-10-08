package com.example.amulet.core.network.auth

fun interface AppCheckTokenProvider {
    suspend fun getAppCheckToken(): String?
}
