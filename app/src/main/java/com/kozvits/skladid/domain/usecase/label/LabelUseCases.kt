package com.kozvits.skladid.domain.usecase.label

import android.graphics.Bitmap
import com.kozvits.skladid.domain.model.BluetoothPrinterInfo
import com.kozvits.skladid.domain.model.Product
import com.kozvits.skladid.domain.repository.LabelRepository
import com.kozvits.skladid.domain.repository.LabelSettings
import com.kozvits.skladid.domain.repository.PrinterRepository
import com.kozvits.skladid.domain.repository.PrinterSettings
import javax.inject.Inject

class GenerateLabelUseCase @Inject constructor(
    private val labelRepository: LabelRepository
) {
    operator fun invoke(product: Product, settings: LabelSettings): Bitmap =
        labelRepository.generateLabel(product, settings)
}

class PrintLabelUseCase @Inject constructor(
    private val printerRepository: PrinterRepository
) {
    suspend operator fun invoke(
        bitmap: Bitmap,
        printerSettings: PrinterSettings,
        labelSettings: LabelSettings
    ): Result<Unit> = printerRepository.printLabel(bitmap, printerSettings, labelSettings)
}

class ListPairedBluetoothPrintersUseCase @Inject constructor(
    private val printerRepository: PrinterRepository
) {
    operator fun invoke(): Result<List<BluetoothPrinterInfo>> =
        printerRepository.listPairedBluetoothDevices()
}
