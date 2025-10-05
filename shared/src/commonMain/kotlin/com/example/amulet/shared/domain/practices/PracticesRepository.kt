package com.example.amulet.shared.domain.practices

import com.example.amulet.shared.core.AppResult

interface PracticesRepository {
    suspend fun loadPractices(): AppResult<Unit>
}
