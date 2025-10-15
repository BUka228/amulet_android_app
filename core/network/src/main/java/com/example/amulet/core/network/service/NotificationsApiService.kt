package com.example.amulet.core.network.service

import com.example.amulet.core.network.dto.notifications.UserPushTokenRequestDto
import com.example.amulet.core.network.dto.notifications.UserPushTokensResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NotificationsApiService {

    @GET("users.pushToken")
    suspend fun getPushTokens(): UserPushTokensResponseDto

    @POST("users.pushToken")
    suspend fun registerPushToken(@Body request: UserPushTokenRequestDto): UserPushTokensResponseDto

    @DELETE("users.pushToken/{tokenId}")
    suspend fun deletePushToken(@Path("tokenId") tokenId: String): UserPushTokensResponseDto
}
