package com.kozvits.skladid.domain.usecase.recognition

import com.kozvits.skladid.domain.model.LocalRecognitionResult
import com.kozvits.skladid.domain.repository.LocalRecognitionRepository
import javax.inject.Inject

class RecognizeLocalUseCase @Inject constructor(
    private val localRecognitionRepository: LocalRecognitionRepository
) {
    suspend operator fun invoke(tagPhotoPath: String): Result<LocalRecognitionResult> =
        localRecognitionRepository.recognize(tagPhotoPath)
}
