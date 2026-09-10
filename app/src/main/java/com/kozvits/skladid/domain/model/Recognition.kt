package com.kozvits.skladid.domain.model

/** Result of the on-device ML Kit pass (barcode + OCR) before any cloud call. */
data class LocalRecognitionResult(
    val barcode: String? = null,
    val recognizedText: String? = null
) {
    val hasAnyData: Boolean get() = !barcode.isNullOrBlank() || !recognizedText.isNullOrBlank()
}

/** Structured product data as returned by the OpenRouter vision model. */
data class AiProductGuess(
    val name: String,
    val manufacturer: String,
    val category: String,
    val specs: String
)

/** A vision-capable model available on OpenRouter, for the model picker in Settings. */
data class OpenRouterModelInfo(
    val id: String,
    val displayName: String,
    val supportsVision: Boolean
)
