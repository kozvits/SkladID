package com.kozvits.skladid.presentation.recognition

import com.kozvits.skladid.domain.model.QuantityUnit

data class RecognitionSubmission(
    val name: String,
    val manufacturer: String,
    val category: String,
    val specs: String,
    val applicability: String,
    val barcode: String?,
    val recognizedText: String?,
    val quantity: Double,
    val unit: QuantityUnit,
    val rack: String,
    val cell: String
)
