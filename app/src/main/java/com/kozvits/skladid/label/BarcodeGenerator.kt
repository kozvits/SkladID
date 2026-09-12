package com.kozvits.skladid.label

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.oned.Code128Writer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BarcodeGenerator @Inject constructor() {

    /** Renders [data] as a Code128 barcode bitmap of the requested pixel size. Falls back to a
     * blank bitmap if [data] is empty or not encodable (e.g. contains unsupported characters). */
    fun generateCode128(data: String, widthPx: Int, heightPx: Int): Bitmap {
        val safeWidth = widthPx.coerceAtLeast(1)
        val safeHeight = heightPx.coerceAtLeast(1)

        if (data.isBlank()) return Bitmap.createBitmap(safeWidth, safeHeight, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.WHITE)
        }

        return try {
            val matrix = Code128Writer().encode(data, BarcodeFormat.CODE_128, safeWidth, safeHeight)
            val bitmap = Bitmap.createBitmap(safeWidth, safeHeight, Bitmap.Config.ARGB_8888)
            for (x in 0 until safeWidth) {
                for (y in 0 until safeHeight) {
                    bitmap.setPixel(x, y, if (matrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            Bitmap.createBitmap(safeWidth, safeHeight, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.WHITE) }
        }
    }
}
