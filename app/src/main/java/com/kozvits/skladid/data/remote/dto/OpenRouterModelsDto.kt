package com.kozvits.skladid.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenRouterModelsResponseDto(
    val data: List<OpenRouterModelDto> = emptyList()
)

@Serializable
data class OpenRouterModelDto(
    val id: String,
    val name: String? = null,
    @SerialName("architecture") val architecture: OpenRouterArchitectureDto? = null
)

@Serializable
data class OpenRouterArchitectureDto(
    @SerialName("input_modalities") val inputModalities: List<String>? = null,
    // Older API responses expose modality as a single combined string, e.g. "text+image->text".
    val modality: String? = null
) {
    val supportsVision: Boolean
        get() = inputModalities?.any { it.equals("image", ignoreCase = true) } == true ||
            modality?.contains("image", ignoreCase = true) == true
}
