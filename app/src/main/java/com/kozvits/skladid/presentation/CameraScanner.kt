package com.kozvits.skladid.presentation

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.barcode.BarcodeScanning

/**
 * Сканирует штрих-код с изображения через ML Kit.
 * Возвращает первый распознанный штриховой код или null.
 */
fun scanBarcode(bitmap: Bitmap): String? {
    val image = InputImage.fromBitmap(bitmap, 0)
    val scanner = BarcodeScanning.getClient()
    val barcodes = scanner.process(image).get()
    return barcodes.firstOrNull()?.rawValue
}