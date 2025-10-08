package com.example.amulet.core.network.dto.rules

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class RuleDto(
    val id: String,
    val ownerId: String? = null,
    val trigger: RuleTriggerDto? = null,
    val action: RuleActionDto? = null,
    val enabled: Boolean? = null,
    val schedule: JsonObject? = null
)

@Serializable
data class RuleTriggerDto(
    val type: String,
    val params: JsonObject? = null
)

@Serializable
data class RuleActionDto(
    val type: String,
    val params: JsonObject? = null
)

@Serializable
data class RuleCreateRequestDto(
    val trigger: RuleTriggerDto,
    val action: RuleActionDto,
    val schedule: JsonObject? = null,
    val enabled: Boolean
)

@Serializable
data class RuleUpdateRequestDto(
    val trigger: RuleTriggerDto? = null,
    val action: RuleActionDto? = null,
    val schedule: JsonObject? = null,
    val enabled: Boolean? = null
)

@Serializable
data class RuleResponseDto(
    val rule: RuleDto
)

@Serializable
data class RuleListResponseDto(
    val rules: List<RuleDto> = emptyList()
)
