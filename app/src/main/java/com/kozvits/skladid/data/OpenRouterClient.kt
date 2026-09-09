package com.kozvits.skladid.data

class OpenRouterClient(private val apiKey: String) {
    private val client = okhttp3.OkHttpClient()

    fun fetchModels(): List<String> {
        // TODO: GET https://openrouter.ai/api/v1/models
        return listOf("openrouter/auto", "google/gemini-2.0-flash")
    }

    fun refreshModels(): List<String> = fetchModels()
}