package com.example.amulet.data.hugs.datasource.remote

import com.example.amulet.core.network.NetworkExceptionMapper
import com.example.amulet.core.network.dto.hug.HugDto
import com.example.amulet.core.network.dto.hug.HugListResponseDto
import com.example.amulet.core.network.dto.hug.HugSendRequestDto
import com.example.amulet.core.network.dto.hug.HugStatusUpdateRequestDto
import com.example.amulet.core.network.safeApiCall
import com.example.amulet.core.network.service.HugsApiService
import com.example.amulet.shared.core.AppResult
import javax.inject.Inject
import javax.inject.Singleton

interface HugsRemoteDataSource {

    suspend fun sendHug(request: HugSendRequestDto): AppResult<String>

    suspend fun getHug(hugId: String): AppResult<HugDto>

    suspend fun updateHugStatus(hugId: String, status: String): AppResult<HugDto>

    suspend fun getHugs(
        direction: String,
        cursor: String? = null,
        limit: Int? = null
    ): AppResult<HugListResponseDto>
}

@Singleton
class HugsRemoteDataSourceImpl @Inject constructor(
    private val apiService: HugsApiService,
    private val exceptionMapper: NetworkExceptionMapper
) : HugsRemoteDataSource {

    override suspend fun sendHug(request: HugSendRequestDto): AppResult<String> =
        safeApiCall(exceptionMapper) { apiService.sendHug(request).hugId }

    override suspend fun getHug(hugId: String): AppResult<HugDto> =
        safeApiCall(exceptionMapper) { apiService.getHug(hugId).hug }

    override suspend fun updateHugStatus(hugId: String, status: String): AppResult<HugDto> =
        safeApiCall(exceptionMapper) {
            apiService.updateHugStatus(hugId, HugStatusUpdateRequestDto(status)).hug
        }

    override suspend fun getHugs(
        direction: String,
        cursor: String?,
        limit: Int?
    ): AppResult<HugListResponseDto> =
        safeApiCall(exceptionMapper) { apiService.getHugs(direction, cursor, limit) }
}
