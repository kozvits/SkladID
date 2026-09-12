package com.kozvits.skladid.domain.repository

import com.kozvits.skladid.domain.model.Product

interface TelegramRepository {
    /**
     * Sends the full [products] list to the configured Telegram chat as a JSON document
     * (sendDocument) followed by a human-readable text summary (sendMessage).
     */
    suspend fun sendProductList(products: List<Product>, botToken: String, chatId: String): Result<Unit>
}
