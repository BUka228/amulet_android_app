package com.example.amulet.core.network.service

import com.example.amulet.core.network.dto.pair.PairAcceptRequestDto
import com.example.amulet.core.network.dto.pair.PairDto
import com.example.amulet.core.network.dto.pair.PairInviteRequestDto
import com.example.amulet.core.network.dto.pair.PairInviteResponseDto
import com.example.amulet.core.network.dto.pair.PairListResponseDto
import com.example.amulet.core.network.dto.pair.PairResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PairsApiService {

    @POST("pairs.invite")
    suspend fun invitePair(@Body request: PairInviteRequestDto): PairInviteResponseDto

    @POST("pairs.accept")
    suspend fun acceptPair(@Body request: PairAcceptRequestDto): PairResponseDto

    @GET("pairs")
    suspend fun getPairs(): PairListResponseDto

    @POST("pairs/{pairId}/block")
    suspend fun blockPair(@Path("pairId") pairId: String): PairResponseDto

    @POST("pairs/{pairId}/unblock")
    suspend fun unblockPair(@Path("pairId") pairId: String): PairResponseDto
}
