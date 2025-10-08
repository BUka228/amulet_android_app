package com.example.amulet.core.network.service

import com.example.amulet.core.network.dto.privacy.PrivacyDeletionCreateResponseDto
import com.example.amulet.core.network.dto.privacy.PrivacyExportCreateResponseDto
import com.example.amulet.core.network.dto.privacy.PrivacyExportStatusResponseDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PrivacyApiService {

    @POST("privacy/export")
    suspend fun requestExport(): PrivacyExportCreateResponseDto

    @GET("privacy/export/{jobId}")
    suspend fun getExportStatus(@Path("jobId") jobId: String): PrivacyExportStatusResponseDto

    @POST("privacy/delete")
    suspend fun requestDeletion(): PrivacyDeletionCreateResponseDto
}
