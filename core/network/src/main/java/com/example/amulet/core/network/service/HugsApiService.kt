package com.example.amulet.core.network.service

import com.example.amulet.core.network.dto.hug.HugListResponseDto
import com.example.amulet.core.network.dto.hug.HugResponseDto
import com.example.amulet.core.network.dto.hug.HugSendRequestDto
import com.example.amulet.core.network.dto.hug.HugSendResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface HugsApiService {

    @POST("hugs.send")
    suspend fun sendHug(@Body request: HugSendRequestDto): HugSendResponseDto

    @GET("hugs")
    suspend fun getHugs(
        @Query("direction") direction: String,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int? = null
    ): HugListResponseDto

    @GET("hugs/{hugId}")
    suspend fun getHug(@Path("hugId") hugId: String): HugResponseDto
}
