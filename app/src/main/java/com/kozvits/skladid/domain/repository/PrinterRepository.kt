package com.kozvits.skladid.domain.repository

import android.graphics.Bitmap
import com.kozvits.skladid.domain.model.BluetoothPrinterInfo

interface PrinterRepository {
    /** Lists paired Bluetooth devices (filtering is best-effort; TSC printers aren't always self-describing). */
    fun listPairedBluetoothDevices(): Result<List<BluetoothPrinterInfo>>

    /** Sends [bitmap] to the printer configured in [printerSettings] as TSPL commands sized per [labelSettings]. */
    suspend fun printLabel(
        bitmap: Bitmap,
        printerSettings: PrinterSettings,
        labelSettings: LabelSettings
    ): Result<Unit>
}
