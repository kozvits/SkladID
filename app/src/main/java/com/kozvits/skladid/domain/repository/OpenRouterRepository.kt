package com.kozvits.skladid.domain.repository

import com.kozvits.skladid.domain.model.AiProductGuess
import com.kozvits.skladid.domain.model.OpenRouterModelInfo

interface OpenRouterRepository {
    /** GET /api/v1/models — returns only vision-capable models. */
    suspend fun fetchVisionModels(): Result<List<OpenRouterModelInfo>>

    /**
     * POST /api/v1/chat/completions with the item photo (base64) and any text/barcode
     * already recognized on-device, asking the model to identify the product.
     */
    suspend fun identifyProduct(
        modelId: String,
        imageBase64: String,
        recognizedTextHint: String?,
        barcodeHint: String?
    ): Result<AiProductGuess>
}
