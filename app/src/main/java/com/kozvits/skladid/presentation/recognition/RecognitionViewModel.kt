package com.kozvits.skladid.presentation.recognition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kozvits.skladid.data.local.ImageBase64Encoder
import com.kozvits.skladid.domain.usecase.recognition.IdentifyProductWithAiUseCase
import com.kozvits.skladid.domain.usecase.recognition.RecognizeLocalUseCase
import com.kozvits.skladid.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecognitionFields(
    val name: String = "",
    val manufacturer: String = "",
    val category: String = "",
    val specs: String = "",
    val barcode: String? = null,
    val recognizedText: String? = null
)

@HiltViewModel
class RecognitionViewModel @Inject constructor(
    private val recognizeLocal: RecognizeLocalUseCase,
    private val identifyWithAi: IdentifyProductWithAiUseCase,
    private val imageBase64Encoder: ImageBase64Encoder
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<RecognitionFields>>(UiState.Loading)
    val uiState: StateFlow<UiState<RecognitionFields>> = _uiState

    fun startRecognition(itemPhotoPath: String, tagPhotoPath: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            val localResult = recognizeLocal(tagPhotoPath).getOrNull()

            // ML Kit only ever gives us a barcode and/or raw OCR text — never structured
            // name/manufacturer/category/specs. Those always come from the AI call below;
            // the local result is passed along purely as a hint to help the model.
            runCatching { imageBase64Encoder.encodeDownscaled(itemPhotoPath) }
                .onSuccess { base64 ->
                    identifyWithAi(
                        imageBase64 = base64,
                        recognizedTextHint = localResult?.recognizedText,
                        barcodeHint = localResult?.barcode
                    ).onSuccess { guess ->
                        _uiState.value = UiState.Success(
                            RecognitionFields(
                                name = guess.name,
                                manufacturer = guess.manufacturer,
                                category = guess.category,
                                specs = guess.specs,
                                barcode = localResult?.barcode,
                                recognizedText = localResult?.recognizedText
                            )
                        )
                    }.onFailure { error ->
                        _uiState.value = UiState.Error(error.message ?: "Ошибка распознавания ИИ")
                    }
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Не удалось подготовить изображение")
                }
        }
    }

    fun retry(itemPhotoPath: String, tagPhotoPath: String) = startRecognition(itemPhotoPath, tagPhotoPath)
}
