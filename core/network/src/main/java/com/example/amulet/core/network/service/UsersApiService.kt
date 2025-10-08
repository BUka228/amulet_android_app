package com.example.amulet.core.network.service

import com.example.amulet.core.network.dto.user.PrivacyRightsResponseDto
import com.example.amulet.core.network.dto.user.UserInitRequestDto
import com.example.amulet.core.network.dto.user.UserResponseDto
import com.example.amulet.core.network.dto.user.UserUpdateRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PATCH

interface UsersApiService {

    @POST("users.me.init")
    suspend fun initUser(@Body request: UserInitRequestDto = UserInitRequestDto()): UserResponseDto

    @GET("users.me")
    suspend fun getCurrentUser(): UserResponseDto

    @PATCH("users.me")
    suspend fun updateCurrentUser(@Body request: UserUpdateRequestDto): UserResponseDto

    @GET("privacy/rights")
    suspend fun getPrivacyRights(): PrivacyRightsResponseDto
}
