package com.kozvits.skladid.domain.repository

import com.kozvits.skladid.domain.model.LocalRecognitionResult

interface LocalRecognitionRepository {
    /** Runs ML Kit barcode scanning + text recognition on a captured tag photo. */
    suspend fun recognize(imagePath: String): Result<LocalRecognitionResult>
}
