package com.example.amulet.shared.domain.hugs

interface HugsRepository {
    suspend fun sendHug(): Result<Unit>
}
