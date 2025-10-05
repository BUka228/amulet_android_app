package com.example.amulet.shared.domain.devices

interface DevicesRepository {
    suspend fun syncDevices(): Result<Unit>
}
