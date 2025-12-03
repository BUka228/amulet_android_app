package com.example.amulet.data.hugs.datasource.remote

import com.example.amulet.core.network.NetworkExceptionMapper
import com.example.amulet.core.network.dto.pair.PairEmotionDto
import com.example.amulet.core.network.dto.pair.PairEmotionListResponseDto
import com.example.amulet.core.network.dto.pair.PairEmotionUpdateRequestDto
import com.example.amulet.core.network.dto.pair.PairAcceptRequestDto
import com.example.amulet.core.network.dto.pair.PairInviteRequestDto
import com.example.amulet.core.network.dto.pair.PairInviteResponseDto
import com.example.amulet.core.network.dto.pair.PairMemberSettingsDto
import com.example.amulet.core.network.dto.pair.PairMemberSettingsUpdateRequestDto
import com.example.amulet.core.network.dto.pair.PairListResponseDto
import com.example.amulet.core.network.dto.pair.PairQuickReplyDto
import com.example.amulet.core.network.dto.pair.PairQuickReplyListResponseDto
import com.example.amulet.core.network.dto.pair.PairQuickReplyUpdateRequestDto
import com.example.amulet.core.network.dto.pair.PairResponseDto
import com.example.amulet.core.network.safeApiCall
import com.example.amulet.core.network.service.PairsApiService
import com.example.amulet.shared.core.AppResult
import javax.inject.Inject
import javax.inject.Singleton

interface PairsRemoteDataSource {

    suspend fun invitePair(method: String, target: String? = null): AppResult<PairInviteResponseDto>

    suspend fun acceptPair(inviteId: String): AppResult<PairResponseDto>

    suspend fun getPairs(): AppResult<PairListResponseDto>

    suspend fun getPairEmotions(pairId: String): AppResult<PairEmotionListResponseDto>

    suspend fun updatePairEmotions(pairId: String, emotions: List<PairEmotionDto>): AppResult<PairEmotionListResponseDto>

    suspend fun getQuickReplies(pairId: String, userId: String): AppResult<PairQuickReplyListResponseDto>

    suspend fun updateQuickReplies(pairId: String, replies: List<PairQuickReplyDto>): AppResult<PairQuickReplyListResponseDto>

    suspend fun updateMemberSettings(
        pairId: String,
        userId: String,
        settings: PairMemberSettingsDto
    ): AppResult<PairResponseDto>

    suspend fun blockPair(pairId: String): AppResult<PairResponseDto>

    suspend fun unblockPair(pairId: String): AppResult<PairResponseDto>
}

@Singleton
class PairsRemoteDataSourceImpl @Inject constructor(
    private val apiService: PairsApiService,
    private val exceptionMapper: NetworkExceptionMapper
) : PairsRemoteDataSource {
    override suspend fun invitePair(method: String, target: String?): AppResult<PairInviteResponseDto> =
        safeApiCall(exceptionMapper) {
            apiService.invitePair(PairInviteRequestDto(method = method, target = target))
        }

    override suspend fun acceptPair(inviteId: String): AppResult<PairResponseDto> =
        safeApiCall(exceptionMapper) {
            apiService.acceptPair(PairAcceptRequestDto(inviteId = inviteId))
        }

    override suspend fun getPairs(): AppResult<PairListResponseDto> =
        safeApiCall(exceptionMapper) { apiService.getPairs() }

    override suspend fun getPairEmotions(pairId: String): AppResult<PairEmotionListResponseDto> =
        safeApiCall(exceptionMapper) { apiService.getPairEmotions(pairId) }

    override suspend fun updatePairEmotions(
        pairId: String,
        emotions: List<PairEmotionDto>
    ): AppResult<PairEmotionListResponseDto> =
        safeApiCall(exceptionMapper) {
            apiService.updatePairEmotions(pairId, PairEmotionUpdateRequestDto(emotions))
        }

    override suspend fun getQuickReplies(
        pairId: String,
        userId: String
    ): AppResult<PairQuickReplyListResponseDto> =
        safeApiCall(exceptionMapper) { apiService.getQuickReplies(pairId, userId) }

    override suspend fun updateQuickReplies(
        pairId: String,
        replies: List<PairQuickReplyDto>
    ): AppResult<PairQuickReplyListResponseDto> =
        safeApiCall(exceptionMapper) {
            apiService.updateQuickReplies(pairId, PairQuickReplyUpdateRequestDto(replies))
        }

    override suspend fun updateMemberSettings(
        pairId: String,
        userId: String,
        settings: PairMemberSettingsDto
    ): AppResult<PairResponseDto> =
        safeApiCall(exceptionMapper) {
            apiService.updateMemberSettings(pairId, userId, PairMemberSettingsUpdateRequestDto(settings))
        }

    override suspend fun blockPair(pairId: String): AppResult<PairResponseDto> =
        safeApiCall(exceptionMapper) {
            apiService.blockPair(pairId)
        }

    override suspend fun unblockPair(pairId: String): AppResult<PairResponseDto> =
        safeApiCall(exceptionMapper) {
            apiService.unblockPair(pairId)
        }
}
