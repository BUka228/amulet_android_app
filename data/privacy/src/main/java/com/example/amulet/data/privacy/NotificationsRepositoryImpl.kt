package com.example.amulet.data.privacy

import com.example.amulet.core.network.NetworkExceptionMapper
import com.example.amulet.core.network.dto.notifications.UserPushTokenRequestDto
import com.example.amulet.core.network.safeApiCall
import com.example.amulet.core.network.service.NotificationsApiService
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.logging.Logger
import com.example.amulet.shared.domain.notifications.NotificationsRepository
import com.github.michaelbull.result.Ok
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsRepositoryImpl @Inject constructor(
    private val apiService: NotificationsApiService,
    private val exceptionMapper: NetworkExceptionMapper
) : NotificationsRepository {

    override suspend fun syncPushToken(token: String?, notificationsAllowed: Boolean): AppResult<Unit> {
        // Простой кэш для дедупликации одинаковых запросов.
        if (token == lastSyncedToken && notificationsAllowed == lastNotificationsAllowed) {
            return Ok(Unit)
        }

        // Если пользователь запретил уведомления, удаляем все токены и не регистрируем новые.
        if (!notificationsAllowed) {
            Logger.d("Notifications disabled by user, removing push tokens", tag = TAG)
            return safeApiCall(exceptionMapper) {
                val response = apiService.getPushTokens()
                response.tokens.forEach { existing ->
                    apiService.deletePushToken(existing.id)
                }
                lastSyncedToken = null
                lastNotificationsAllowed = false
                Ok(Unit)
            }
        }

        return if (token == null) {
            // Токен отсутствует, но уведомления разрешены — подчистим возможные старые токены.
            safeApiCall(exceptionMapper) {
                val response = apiService.getPushTokens()
                response.tokens.forEach { existing ->
                    apiService.deletePushToken(existing.id)
                }
                lastSyncedToken = null
                lastNotificationsAllowed = true
                Ok(Unit)
            }
        } else {
            safeApiCall(exceptionMapper) {
                val response = apiService.getPushTokens()
                val exists = response.tokens.any { it.token == token }
                if (!exists) {
                    apiService.registerPushToken(
                        UserPushTokenRequestDto(
                            token = token,
                            platform = "android"
                        )
                    )
                }
                lastSyncedToken = token
                lastNotificationsAllowed = true
                Ok(Unit)
            }
        }
    }

    companion object {
        private const val TAG = "NotificationsRepositoryImpl"
    }

    private var lastSyncedToken: String? = null
    private var lastNotificationsAllowed: Boolean? = null
}
