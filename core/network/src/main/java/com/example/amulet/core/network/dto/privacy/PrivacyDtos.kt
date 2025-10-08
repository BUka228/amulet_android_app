package com.example.amulet.core.network.dto.privacy

import com.example.amulet.core.network.dto.common.JobStatusDto
import kotlinx.serialization.Serializable

@Serializable
data class PrivacyExportCreateResponseDto(
    val success: Boolean,
    val message: String? = null,
    val data: PrivacyJobDataDto? = null
)

@Serializable
data class PrivacyDeletionCreateResponseDto(
    val success: Boolean,
    val message: String? = null,
    val data: PrivacyJobDataDto? = null
)

@Serializable
data class PrivacyJobDataDto(
    val jobId: String,
    val status: String,
    val estimatedCompletionTime: String? = null,
    val warning: String? = null
)

@Serializable
data class PrivacyExportStatusResponseDto(
    val success: Boolean,
    val data: JobStatusDto? = null
)
