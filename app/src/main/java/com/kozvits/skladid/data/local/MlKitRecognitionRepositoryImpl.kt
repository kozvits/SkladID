package com.kozvits.skladid.data.local

import android.graphics.BitmapFactory
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.kozvits.skladid.domain.model.LocalRecognitionResult
import com.kozvits.skladid.domain.repository.LocalRecognitionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MlKitRecognitionRepositoryImpl @Inject constructor() : LocalRecognitionRepository {

    private val barcodeScanner by lazy { BarcodeScanning.getClient() }
    private val textRecognizer by lazy { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    override suspend fun recognize(imagePath: String): Result<LocalRecognitionResult> =
        withContext(Dispatchers.Default) {
            runCatching {
                val bitmap = BitmapFactory.decodeFile(imagePath)
                    ?: throw IllegalArgumentException("Не удалось загрузить изображение: $imagePath")
                val inputImage = InputImage.fromBitmap(bitmap, 0)

                val barcodes = barcodeScanner.process(inputImage).await()
                val barcodeValue = barcodes.firstOrNull()?.rawValue ?: barcodes.firstOrNull()?.displayValue

                val textResult = textRecognizer.process(inputImage).await()
                val recognizedText = textResult.text.takeIf { it.isNotBlank() }

                LocalRecognitionResult(
                    barcode = barcodeValue,
                    recognizedText = recognizedText
                )
            }
        }
}
