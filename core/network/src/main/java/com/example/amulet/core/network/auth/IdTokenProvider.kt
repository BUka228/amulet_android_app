package com.example.amulet.core.network.auth

fun interface IdTokenProvider {
    suspend fun getIdToken(): String?
}
