package com.kozvits.skladid.printer

import android.graphics.Bitmap

/**
 * Builds TSC TSPL command byte streams from a rendered label bitmap.
 *
 * [buildFromPixels] is the pure, unit-testable core: it takes a raw ARGB pixel array (as returned
 * by [Bitmap.getPixels]) rather than a live [Bitmap], so tests can exercise it on the JVM without
 * an Android runtime. [build] is the thin Android-facing wrapper used in production.
 */
object TsplCommandBuilder {

    private const val BLACK_THRESHOLD = 128
    private const val DEFAULT_GAP_MM = 2f

    fun build(bitmap: Bitmap, labelWidthMm: Float, labelHeightMm: Float): ByteArray {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        return buildFromPixels(pixels, bitmap.width, bitmap.height, labelWidthMm, labelHeightMm)
    }

    fun buildFromPixels(
        pixels: IntArray,
        width: Int,
        height: Int,
        labelWidthMm: Float,
        labelHeightMm: Float
    ): ByteArray {
        require(pixels.size == width * height) { "pixels size must equal width*height" }
        require(width > 0 && height > 0) { "width/height must be positive" }

        val widthBytes = (width + 7) / 8
        val packed = packMonochrome(pixels, width, height, widthBytes)

        val header = buildHeader(labelWidthMm, labelHeightMm)
        val bitmapCommandPrefix = "BITMAP 0,0,$widthBytes,$height,0,".toByteArray(Charsets.US_ASCII)
        val footer = "\r\nPRINT 1,1\r\n".toByteArray(Charsets.US_ASCII)

        return header + bitmapCommandPrefix + packed + footer
    }

    private fun buildHeader(labelWidthMm: Float, labelHeightMm: Float): ByteArray {
        val widthStr = formatMm(labelWidthMm)
        val heightStr = formatMm(labelHeightMm)
        return buildString {
            append("SIZE $widthStr mm,$heightStr mm\r\n")
            append("GAP ${formatMm(DEFAULT_GAP_MM)} mm,0 mm\r\n")
            append("CLS\r\n")
        }.toByteArray(Charsets.US_ASCII)
    }

    private fun formatMm(value: Float): String {
        // TSPL accepts plain decimal values; trim a trailing ".0" for whole millimetres.
        return if (value == value.toInt().toFloat()) value.toInt().toString() else value.toString()
    }

    /**
     * Packs [pixels] (row-major ARGB, [width]x[height]) into a 1-bit-per-pixel, MSB-first,
     * byte-padded-per-row buffer where a set bit means "print black" — the convention TSPL's
     * BITMAP command expects.
     */
    private fun packMonochrome(pixels: IntArray, width: Int, height: Int, widthBytes: Int): ByteArray {
        val output = ByteArray(widthBytes * height)
        for (y in 0 until height) {
            val rowOffset = y * width
            val outRowOffset = y * widthBytes
            for (x in 0 until width) {
                val pixel = pixels[rowOffset + x]
                if (isBlack(pixel)) {
                    val byteIndex = outRowOffset + (x / 8)
                    val bitIndex = 7 - (x % 8)
                    output[byteIndex] = (output[byteIndex].toInt() or (1 shl bitIndex)).toByte()
                }
            }
        }
        return output
    }

    private fun isBlack(pixel: Int): Boolean {
        val r = (pixel shr 16) and 0xFF
        val g = (pixel shr 8) and 0xFF
        val b = pixel and 0xFF
        val luminance = 0.299 * r + 0.587 * g + 0.114 * b
        return luminance < BLACK_THRESHOLD
    }
}
