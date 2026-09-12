package com.kozvits.skladid.telegram

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

class TelegramApiException(message: String) : Exception(message)

private const val TELEGRAM_API_BASE = "https://api.telegram.org"

@Singleton
class TelegramApiClient @Inject constructor(
    private val okHttpClient: OkHttpClient
) {

    suspend fun sendDocument(
        botToken: String,
        chatId: String,
        file: File,
        filename: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("chat_id", chatId)
                .addFormDataPart(
                    "document",
                    filename,
                    file.asRequestBody("application/json".toMediaType())
                )
                .build()

            val request = Request.Builder()
                .url("$TELEGRAM_API_BASE/bot$botToken/sendDocument")
                .post(body)
                .build()

            execute(request)
        }
    }

    suspend fun sendMessage(botToken: String, chatId: String, text: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("chat_id", chatId)
                    .addFormDataPart("text", text)
                    .build()

                val request = Request.Builder()
                    .url("$TELEGRAM_API_BASE/bot$botToken/sendMessage")
                    .post(body)
                    .build()

                execute(request)
            }
        }

    private fun execute(request: Request) {
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string().orEmpty()
                throw TelegramApiException("Telegram API HTTP ${response.code}: $errorBody")
            }
        }
    }
}
