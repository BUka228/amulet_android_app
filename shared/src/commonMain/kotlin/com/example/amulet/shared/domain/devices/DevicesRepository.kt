package com.example.amulet.shared.domain.devices

import com.example.amulet.shared.core.AppResult

interface DevicesRepository {
    suspend fun syncDevices(): AppResult<Unit>
}
