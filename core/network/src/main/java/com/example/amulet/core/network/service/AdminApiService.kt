package com.example.amulet.core.network.service

import com.example.amulet.core.network.dto.admin.AdminStatsOverviewResponseDto
import retrofit2.http.GET

interface AdminApiService {

    @GET("admin/stats.overview")
    suspend fun getStatsOverview(): AdminStatsOverviewResponseDto
}
