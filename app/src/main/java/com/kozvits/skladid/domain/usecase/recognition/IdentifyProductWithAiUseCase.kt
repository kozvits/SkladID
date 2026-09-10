package com.kozvits.skladid.domain.usecase.recognition

import com.kozvits.skladid.domain.model.AiProductGuess
import com.kozvits.skladid.domain.repository.OpenRouterRepository
import com.kozvits.skladid.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class IdentifyProductWithAiUseCase @Inject constructor(
    private val openRouterRepository: OpenRouterRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(
        imageBase64: String,
        recognizedTextHint: String?,
        barcodeHint: String?
    ): Result<AiProductGuess> {
        val modelId = settingsRepository.observeSelectedModelId().first()
            ?: return Result.failure(IllegalStateException("Модель распознавания не выбрана в настройках"))

        return openRouterRepository.identifyProduct(
            modelId = modelId,
            imageBase64 = imageBase64,
            recognizedTextHint = recognizedTextHint,
            barcodeHint = barcodeHint
        )
    }
}
