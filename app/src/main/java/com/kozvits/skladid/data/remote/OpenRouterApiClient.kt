package com.kozvits.skladid.data.remote

import com.kozvits.skladid.data.remote.dto.ChatCompletionRequestDto
import com.kozvits.skladid.data.remote.dto.ChatCompletionResponseDto
import com.kozvits.skladid.data.remote.dto.ChatContentPartDto
import com.kozvits.skladid.data.remote.dto.ChatMessageDto
import com.kozvits.skladid.data.remote.dto.ImageUrlDto
import com.kozvits.skladid.data.remote.dto.OpenRouterModelsResponseDto
import com.kozvits.skladid.data.remote.dto.ProductGuessDto
import com.kozvits.skladid.domain.model.AiProductGuess
import com.kozvits.skladid.domain.model.OpenRouterModelInfo
import com.kozvits.skladid.domain.repository.OpenRouterRepository
import com.kozvits.skladid.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val BASE_URL = "https://openrouter.ai/api/v1"
private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

@Singleton
class OpenRouterApiClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val settingsRepository: SettingsRepository
) : OpenRouterRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    override suspend fun fetchVisionModels(): Result<List<OpenRouterModelInfo>> = withContext(Dispatchers.IO) {
        runCatching {
            val apiKey = requireApiKey()
            val request = Request.Builder()
                .url("$BASE_URL/models")
                .header("Authorization", "Bearer $apiKey")
                .get()
                .build()

            val body = executeOrThrow(request)
            val parsed = json.decodeFromString(OpenRouterModelsResponseDto.serializer(), body)

            parsed.data
                .filter { it.architecture?.supportsVision == true }
                .map { dto ->
                    OpenRouterModelInfo(
                        id = dto.id,
                        displayName = dto.name ?: dto.id,
                        supportsVision = true
                    )
                }
                .sortedBy { it.displayName }
        }
    }

    override suspend fun identifyProduct(
        modelId: String,
        imageBase64: String,
        recognizedTextHint: String?,
        barcodeHint: String?
    ): Result<AiProductGuess> = withContext(Dispatchers.IO) {
        runCatching {
            val apiKey = requireApiKey()

            val promptText = buildPrompt(recognizedTextHint, barcodeHint)

            val requestDto = ChatCompletionRequestDto(
                model = modelId,
                messages = listOf(
                    ChatMessageDto(
                        role = "user",
                        content = listOf(
                            ChatContentPartDto(type = "text", text = promptText),
                            ChatContentPartDto(
                                type = "image_url",
                                imageUrl = ImageUrlDto(url = "data:image/jpeg;base64,$imageBase64")
                            )
                        )
                    )
                )
            )

            val requestBodyJson = json.encodeToString(ChatCompletionRequestDto.serializer(), requestDto)

            val request = Request.Builder()
                .url("$BASE_URL/chat/completions")
                .header("Authorization", "Bearer $apiKey")
                .header("Content-Type", "application/json")
                .post(requestBodyJson.toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val body = executeOrThrow(request)
            val completion = json.decodeFromString(ChatCompletionResponseDto.serializer(), body)

            completion.error?.let {
                throw OpenRouterApiException(it.message ?: "Неизвестная ошибка OpenRouter")
            }

            val content = completion.choices.firstOrNull()?.message?.content
                ?: throw OpenRouterApiException("Пустой ответ модели")

            val guessDto = try {
                json.decodeFromString(ProductGuessDto.serializer(), content.extractJsonObject())
            } catch (e: Exception) {
                throw OpenRouterApiException("Модель вернула неструктурированный ответ", e)
            }

            AiProductGuess(
                name = guessDto.name,
                manufacturer = guessDto.manufacturer,
                category = guessDto.category,
                specs = guessDto.specs
            )
        }
    }

    private suspend fun requireApiKey(): String =
        settingsRepository.getApiKey()?.takeIf { it.isNotBlank() }
            ?: throw OpenRouterApiException("API-ключ OpenRouter не задан в настройках")

    private fun buildPrompt(recognizedTextHint: String?, barcodeHint: String?): String {
        val hints = buildString {
            if (!barcodeHint.isNullOrBlank()) append("Штрих-код: $barcodeHint. ")
            if (!recognizedTextHint.isNullOrBlank()) append("Распознанный текст с бирки: $recognizedTextHint. ")
        }
        return """
            Определи товар по фотографии. $hints
            Верни СТРОГО JSON без пояснений и без markdown, в формате:
            {"name": "...", "manufacturer": "...", "category": "...", "specs": "..."}
        """.trimIndent()
    }

    /** Some models wrap the JSON in ```json fences despite instructions — strip them defensively. */
    private fun String.extractJsonObject(): String {
        val trimmed = trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        return if (start >= 0 && end > start) trimmed.substring(start, end + 1) else trimmed
    }

    private suspend fun executeOrThrow(request: Request): String = suspendCancellableCall(request) { response ->
        if (!response.isSuccessful) {
            val errorBody = response.body?.string().orEmpty()
            throw OpenRouterApiException("HTTP ${response.code}: $errorBody")
        }
        response.body?.string() ?: throw OpenRouterApiException("Пустое тело ответа")
    }

    private suspend fun <T> suspendCancellableCall(
        request: Request,
        onResponse: (Response) -> T
    ): T = suspendCoroutine { continuation ->
        val call = okHttpClient.newCall(request)
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                continuation.resumeWithException(OpenRouterApiException("Сетевая ошибка: ${e.message}", e))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    try {
                        continuation.resume(onResponse(it))
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                }
            }
        })
    }
}

class OpenRouterApiException(message: String, cause: Throwable? = null) : Exception(message, cause)
