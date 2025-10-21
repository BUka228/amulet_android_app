package com.example.amulet.shared.domain.devices.repository

import com.example.amulet.shared.core.AppResult

interface DevicesRepository {
    suspend fun syncDevices(): AppResult<Unit>
}
