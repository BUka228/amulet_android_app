package com.example.amulet.core.network.dto.pattern

import com.example.amulet.core.network.serialization.ApiTimestamp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
data class PatternDto(
    val id: String,
    val version: Int,
    val ownerId: String? = null,
    val kind: String,
    val spec: PatternSpecDto,
    val public: Boolean? = null,
    val reviewStatus: String? = null,
    val hardwareVersion: Int? = null,
    val title: String? = null,
    val description: String? = null,
    val tags: List<String>? = null,
    val usageCount: Int? = null,
    val sharedWith: List<String>? = null,
    val createdAt: ApiTimestamp? = null,
    val updatedAt: ApiTimestamp? = null
)

@Serializable
data class PatternResponseDto(
    val pattern: PatternDto
)

@Serializable
data class PatternListResponseDto(
    val items: List<PatternDto> = emptyList(),
    val nextCursor: String? = null
)

@Serializable
data class PatternCreateRequestDto(
    val kind: String,
    val spec: PatternSpecDto,
    val title: String? = null,
    val description: String? = null,
    val tags: List<String>? = null,
    val public: Boolean? = null,
    val hardwareVersion: Int
)

@Serializable
data class PatternUpdateRequestDto(
    val version: Int,
    val kind: String? = null,
    val spec: PatternSpecDto? = null,
    val title: String? = null,
    val description: String? = null,
    val tags: List<String>? = null,
    val public: Boolean? = null
)

@Serializable
data class PatternShareRequestDto(
    val toUserId: String? = null,
    val pairId: String? = null
)

@Serializable
data class PatternShareResponseDto(
    val shared: Boolean? = null
)

@Serializable
data class PatternPreviewRequestDto(
    val deviceId: String,
    val spec: PatternSpecDto,
    val duration: Int? = null
)

@Serializable
data class PatternPreviewResponseDto(
    val previewId: String
)

@Serializable
data class PatternDeleteResponseDto(
    val ok: Boolean
)

@Serializable
data class PatternSpecDto(
    val type: String,
    val hardwareVersion: Int,
    val duration: Int? = null,
    val loop: Boolean? = null,
    val elements: List<PatternElementDto> = emptyList()
)

@Serializable
@JsonClassDiscriminator("type")
sealed class PatternElementDto {
    abstract val startTime: Int
    abstract val duration: Int
    abstract val intensity: Double?
    abstract val speed: Double?
}

@Serializable
@SerialName("gradient")
data class PatternElementGradientDto(
    override val startTime: Int,
    override val duration: Int,
    override val intensity: Double? = null,
    override val speed: Double? = null,
    val params: GradientParamsDto
) : PatternElementDto()

@Serializable
data class GradientParamsDto(
    val colors: List<String>,
    val direction: String? = null,
    val leds: List<Int>? = null
)

@Serializable
@SerialName("pulse")
data class PatternElementPulseDto(
    override val startTime: Int,
    override val duration: Int,
    override val intensity: Double? = null,
    override val speed: Double? = null,
    val params: ColorParamsDto
) : PatternElementDto()

@Serializable
data class ColorParamsDto(
    val color: String
)

@Serializable
@SerialName("breathing")
data class PatternElementBreathingDto(
    override val startTime: Int,
    override val duration: Int,
    override val intensity: Double? = null,
    override val speed: Double? = null,
    val params: ColorParamsDto
) : PatternElementDto()

@Serializable
@SerialName("chase")
data class PatternElementChaseDto(
    override val startTime: Int,
    override val duration: Int,
    override val intensity: Double? = null,
    override val speed: Double? = null,
    val params: ChaseParamsDto
) : PatternElementDto()

@Serializable
data class ChaseParamsDto(
    val color: String,
    val leds: List<Int>? = null
)

@Serializable
@SerialName("color")
data class PatternElementColorDto(
    override val startTime: Int,
    override val duration: Int,
    override val intensity: Double? = null,
    override val speed: Double? = null,
    val params: ColorParamsDto
) : PatternElementDto()

@Serializable
@SerialName("custom")
data class PatternElementCustomDto(
    override val startTime: Int,
    override val duration: Int,
    override val intensity: Double? = null,
    override val speed: Double? = null,
    val params: CustomParamsDto
) : PatternElementDto()

@Serializable
data class CustomParamsDto(
    val color: String? = null,
    val colors: List<String>? = null
)

@Serializable
@SerialName("sequence")
data class PatternElementSequenceDto(
    override val startTime: Int,
    override val duration: Int,
    override val intensity: Double? = null,
    override val speed: Double? = null,
    val params: SequenceParamsDto
) : PatternElementDto()

@Serializable
data class SequenceParamsDto(
    val steps: List<SequenceStepDto>
)

@Serializable
@JsonClassDiscriminator("type")
sealed class SequenceStepDto

@Serializable
@SerialName("led")
data class LedActionDto(
    val ledIndex: Int,
    val color: String,
    val durationMs: Int
) : SequenceStepDto()

@Serializable
@SerialName("delay")
data class DelayActionDto(
    val durationMs: Int
) : SequenceStepDto()
