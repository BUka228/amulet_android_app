package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.core.AppResult

interface HugsRepository {
    suspend fun sendHug(): AppResult<Unit>
}
