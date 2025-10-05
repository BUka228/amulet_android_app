package com.example.amulet.shared.core

sealed interface AppError {
    data object Unknown : AppError
    data object Network : AppError
    data object Unauthorized : AppError
}
