package com.example.amulet.shared.domain.notifications.repository

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.notifications.model.PushToken
import com.example.amulet.shared.domain.notifications.model.PushTokenRegistration

interface NotificationsRepository {
    suspend fun registerPushToken(registration: PushTokenRegistration): AppResult<List<PushToken>>
    suspend fun getPushTokens(): AppResult<List<PushToken>>
    suspend fun deletePushToken(tokenId: String): AppResult<List<PushToken>>
}
