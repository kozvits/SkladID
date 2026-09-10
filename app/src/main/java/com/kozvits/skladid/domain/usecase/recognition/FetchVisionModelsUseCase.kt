package com.kozvits.skladid.domain.usecase.recognition

import com.kozvits.skladid.domain.model.OpenRouterModelInfo
import com.kozvits.skladid.domain.repository.OpenRouterRepository
import javax.inject.Inject

class FetchVisionModelsUseCase @Inject constructor(
    private val openRouterRepository: OpenRouterRepository
) {
    suspend operator fun invoke(): Result<List<OpenRouterModelInfo>> =
        openRouterRepository.fetchVisionModels()
}
