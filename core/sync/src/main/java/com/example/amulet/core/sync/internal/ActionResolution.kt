package com.example.amulet.core.sync.internal

sealed interface ActionResolution {
    data object Completed : ActionResolution
    data class Retry(val delayMillis: Long, val errorMessage: String?) : ActionResolution
    data class Failed(val errorMessage: String?) : ActionResolution
}
