package com.example.amulet.core.network.service

import com.example.amulet.core.network.dto.pair.PairAcceptRequestDto
import com.example.amulet.core.network.dto.pair.PairDto
import com.example.amulet.core.network.dto.pair.PairEmotionListResponseDto
import com.example.amulet.core.network.dto.pair.PairEmotionUpdateRequestDto
import com.example.amulet.core.network.dto.pair.PairInviteRequestDto
import com.example.amulet.core.network.dto.pair.PairInviteResponseDto
import com.example.amulet.core.network.dto.pair.PairListResponseDto
import com.example.amulet.core.network.dto.pair.PairMemberSettingsUpdateRequestDto
import com.example.amulet.core.network.dto.pair.PairQuickReplyListResponseDto
import com.example.amulet.core.network.dto.pair.PairQuickReplyUpdateRequestDto
import com.example.amulet.core.network.dto.pair.PairResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PairsApiService {

    @POST("pairs/invite")
    suspend fun invitePair(@Body request: PairInviteRequestDto): PairInviteResponseDto

    @POST("pairs/accept")
    suspend fun acceptPair(@Body request: PairAcceptRequestDto): PairResponseDto

    @GET("pairs")
    suspend fun getPairs(): PairListResponseDto

    @POST("pairs/{pairId}/block")
    suspend fun blockPair(@Path("pairId") pairId: String): PairResponseDto

    @POST("pairs/{pairId}/unblock")
    suspend fun unblockPair(@Path("pairId") pairId: String): PairResponseDto

    @POST("pairs/{pairId}/delete")
    suspend fun deletePair(@Path("pairId") pairId: String)

    @GET("pairs/{pairId}/emotions")
    suspend fun getPairEmotions(
        @Path("pairId") pairId: String
    ): PairEmotionListResponseDto

    @PUT("pairs/{pairId}/emotions")
    suspend fun updatePairEmotions(
        @Path("pairId") pairId: String,
        @Body request: PairEmotionUpdateRequestDto
    ): PairEmotionListResponseDto

    @GET("pairs/{pairId}/quickReplies")
    suspend fun getQuickReplies(
        @Path("pairId") pairId: String,
        @Query("userId") userId: String
    ): PairQuickReplyListResponseDto

    @POST("pairs/{pairId}/quickReplies")
    suspend fun updateQuickReplies(
        @Path("pairId") pairId: String,
        @Body request: PairQuickReplyUpdateRequestDto
    ): PairQuickReplyListResponseDto

    @POST("pairs/{pairId}/members/{userId}/settings")
    suspend fun updateMemberSettings(
        @Path("pairId") pairId: String,
        @Path("userId") userId: String,
        @Body request: PairMemberSettingsUpdateRequestDto
    ): PairResponseDto
}
