package com.example.amulet.core.network.service

import com.example.amulet.core.network.dto.practice.PracticeListResponseDto
import com.example.amulet.core.network.dto.practice.PracticeResponseDto
import com.example.amulet.core.network.dto.practice.PracticeStartRequestDto
import com.example.amulet.core.network.dto.practice.PracticeStopRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PracticesApiService {

    @GET("practices")
    suspend fun getPractices(
        @Query("type") type: String? = null,
        @Query("lang") language: String? = null,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int? = null
    ): PracticeListResponseDto

    @GET("practices/{practiceId}")
    suspend fun getPractice(@Path("practiceId") practiceId: String): PracticeResponseDto

    @POST("practices/{practiceId}/start")
    suspend fun startPractice(
        @Path("practiceId") practiceId: String,
        @Body request: PracticeStartRequestDto
    ): PracticeResponseDto

    @POST("practices/{practiceId}/stop")
    suspend fun stopPractice(
        @Path("practiceId") practiceId: String,
        @Body request: PracticeStopRequestDto
    ): PracticeResponseDto
}
