package com.kozvits.skladid.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionRequestDto(
    val model: String,
    val messages: List<ChatMessageDto>,
    @SerialName("response_format") val responseFormat: ResponseFormatDto? = ResponseFormatDto()
)

@Serializable
data class ResponseFormatDto(val type: String = "json_object")

@Serializable
data class ChatMessageDto(
    val role: String,
    val content: List<ChatContentPartDto>
)

@Serializable
data class ChatContentPartDto(
    val type: String,
    val text: String? = null,
    @SerialName("image_url") val imageUrl: ImageUrlDto? = null
)

@Serializable
data class ImageUrlDto(val url: String)

@Serializable
data class ChatCompletionResponseDto(
    val choices: List<ChatChoiceDto> = emptyList(),
    val error: OpenRouterErrorDto? = null
)

@Serializable
data class ChatChoiceDto(
    val message: ChatResponseMessageDto? = null
)

@Serializable
data class ChatResponseMessageDto(
    val content: String? = null
)

@Serializable
data class OpenRouterErrorDto(
    val message: String? = null,
    val code: Int? = null
)

/** The strict JSON payload we instruct the model to return inside `message.content`. */
@Serializable
data class ProductGuessDto(
    val name: String = "",
    val manufacturer: String = "",
    val category: String = "",
    val specs: String = ""
)
