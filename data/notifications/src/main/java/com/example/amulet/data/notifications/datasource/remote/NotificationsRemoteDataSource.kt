package com.example.amulet.data.notifications.datasource.remote

import com.example.amulet.core.network.NetworkExceptionMapper
import com.example.amulet.core.network.dto.notifications.UserPushTokenDto
import com.example.amulet.core.network.dto.notifications.UserPushTokenRequestDto
import com.example.amulet.core.network.safeApiCall
import com.example.amulet.core.network.service.NotificationsApiService
import com.example.amulet.shared.core.AppResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsRemoteDataSource @Inject constructor(
    private val apiService: NotificationsApiService,
    private val exceptionMapper: NetworkExceptionMapper
) {

    suspend fun registerPushToken(request: UserPushTokenRequestDto): AppResult<List<UserPushTokenDto>> =
        safeApiCall(exceptionMapper) {
            apiService.registerPushToken(request).tokens
        }

    suspend fun deletePushToken(tokenId: String): AppResult<List<UserPushTokenDto>> =
        safeApiCall(exceptionMapper) {
            apiService.deletePushToken(tokenId).tokens
        }

    suspend fun getPushTokens(): AppResult<List<UserPushTokenDto>> =
        safeApiCall(exceptionMapper) {
            apiService.getPushTokens().tokens
        }
}
