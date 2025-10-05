package com.example.amulet.shared.domain.practices

interface PracticesRepository {
    suspend fun loadPractices(): Result<Unit>
}
