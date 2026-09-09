package com.kozvits.skladid.data

import okhttp3.OkHttpClient
import okhttp3.Request

class OpenRouterClient(private val apiKey: String) {
    private val client = OkHttpClient()
    fun fetchModels(): List<String> {
        // placeholder: запрос к OpenRouter /models
        return listOf("openrouter/default", "google/gemini-flash-1.5")
    }
}
