package com.kozvits.skladid.presentation

import android.graphics.Bitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.barcode.BarcodeScanning

/**
 * Сканирует штрих-код с изображения через ML Kit.
 * Возвращает первый распознанный штриховой код или null.
 *
 * Блокирующий вызов (Tasks.await) — вызывать только из фонового потока,
 * не из главного (UI) потока.
 */
fun scanBarcode(bitmap: Bitmap): String? {
    val image = InputImage.fromBitmap(bitmap, 0)
    val scanner = BarcodeScanning.getClient()
    val barcodes = Tasks.await(scanner.process(image))
    return barcodes.firstOrNull()?.rawValue
}