package com.example.amulet.core.network.service

import com.example.amulet.core.network.dto.rules.RuleCreateRequestDto
import com.example.amulet.core.network.dto.rules.RuleListResponseDto
import com.example.amulet.core.network.dto.rules.RuleResponseDto
import com.example.amulet.core.network.dto.rules.RuleUpdateRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface RulesApiService {

    @GET("rules")
    suspend fun getRules(): RuleListResponseDto

    @POST("rules")
    suspend fun createRule(@Body request: RuleCreateRequestDto): RuleResponseDto

    @GET("rules/{id}")
    suspend fun getRule(@Path("id") ruleId: String): RuleResponseDto

    @PATCH("rules/{id}")
    suspend fun updateRule(
        @Path("id") ruleId: String,
        @Body request: RuleUpdateRequestDto
    ): RuleResponseDto

    @DELETE("rules/{id}")
    suspend fun deleteRule(@Path("id") ruleId: String)
}
