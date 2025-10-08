package com.example.amulet.core.network.service

import com.example.amulet.core.network.dto.pattern.PatternCreateRequestDto
import com.example.amulet.core.network.dto.pattern.PatternDeleteResponseDto
import com.example.amulet.core.network.dto.pattern.PatternListResponseDto
import com.example.amulet.core.network.dto.pattern.PatternPreviewRequestDto
import com.example.amulet.core.network.dto.pattern.PatternPreviewResponseDto
import com.example.amulet.core.network.dto.pattern.PatternResponseDto
import com.example.amulet.core.network.dto.pattern.PatternShareRequestDto
import com.example.amulet.core.network.dto.pattern.PatternShareResponseDto
import com.example.amulet.core.network.dto.pattern.PatternUpdateRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface PatternsApiService {

    @GET("patterns")
    suspend fun getPublicPatterns(
        @Query("hardwareVersion") hardwareVersion: Int? = null,
        @Query("kind") kind: String? = null,
        @Query("tags") tags: String? = null,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int? = null
    ): PatternListResponseDto

    @POST("patterns")
    suspend fun createPattern(@Body request: PatternCreateRequestDto): PatternResponseDto

    @GET("patterns.mine")
    suspend fun getOwnPatterns(): PatternListResponseDto

    @GET("patterns/{id}")
    suspend fun getPattern(@Path("id") patternId: String): PatternResponseDto

    @PATCH("patterns/{id}")
    suspend fun updatePattern(
        @Path("id") patternId: String,
        @Body request: PatternUpdateRequestDto
    ): PatternResponseDto

    @DELETE("patterns/{id}")
    suspend fun deletePattern(@Path("id") patternId: String): PatternDeleteResponseDto

    @POST("patterns/{patternId}/share")
    suspend fun sharePattern(
        @Path("patternId") patternId: String,
        @Body request: PatternShareRequestDto
    ): PatternShareResponseDto

    @POST("patterns/preview")
    suspend fun previewPattern(
        @Body request: PatternPreviewRequestDto
    ): PatternPreviewResponseDto
}
