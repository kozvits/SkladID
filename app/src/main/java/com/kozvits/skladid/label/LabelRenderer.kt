package com.kozvits.skladid.label

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.kozvits.skladid.domain.model.Product
import com.kozvits.skladid.domain.repository.LabelSettings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LabelRenderer @Inject constructor(
    private val barcodeGenerator: BarcodeGenerator
) {

    fun render(product: Product, settings: LabelSettings): Bitmap {
        val pxPerMm = settings.dpi / 25.4f
        val widthPx = (settings.widthMm * pxPerMm).toInt().coerceAtLeast(64)
        val heightPx = (settings.heightMm * pxPerMm).toInt().coerceAtLeast(48)

        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val margin = widthPx * 0.04f
        val contentWidth = widthPx - margin * 2

        // --- Title (top ~30% of height), bold, word-wrapped, auto-shrinking font ---
        val titleAreaHeight = heightPx * 0.30f
        val titleLayout = buildAutoSizedLayout(
            text = product.name.ifBlank { "Без названия" },
            maxWidthPx = contentWidth.toInt(),
            maxHeightPx = titleAreaHeight.toInt(),
            startTextSizePx = heightPx * 0.14f,
            minTextSizePx = heightPx * 0.06f,
            bold = true
        )
        canvas.save()
        canvas.translate(margin, margin * 0.5f)
        titleLayout.draw(canvas)
        canvas.restore()

        val middleTop = margin * 0.5f + titleAreaHeight
        val middleHeight = heightPx * 0.48f
        val bottomAreaTop = middleTop + middleHeight

        // --- Barcode block (left ~55% of content width) ---
        val barcodeAreaWidth = (contentWidth * 0.55f).toInt()
        val digitsHeight = heightPx * 0.09f
        val barcodeHeight = (middleHeight - digitsHeight).toInt().coerceAtLeast(1)
        val barcodeValue = product.barcode ?: product.id.toString()
        val barcodeBitmap = barcodeGenerator.generateCode128(barcodeValue, barcodeAreaWidth, barcodeHeight)
        canvas.drawBitmap(barcodeBitmap, margin, middleTop, null)

        val digitsPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = digitsHeight * 0.8f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            barcodeValue,
            margin + barcodeAreaWidth / 2f,
            middleTop + barcodeHeight + digitsHeight * 0.75f,
            digitsPaint
        )

        // --- Storage address block (right ~45% of content width) ---
        val storageBlockLeft = margin + barcodeAreaWidth + contentWidth * 0.05f
        val storageLines = listOf(
            "Склад: ${product.storageAddress.warehouse}",
            "Стеллаж: ${product.storageAddress.rack}",
            "Полка: ${product.storageAddress.shelf}",
            "Ячейка: ${product.storageAddress.cell}"
        )
        val storagePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = heightPx * 0.065f
        }
        var lineY = middleTop + storagePaint.textSize
        for (line in storageLines) {
            canvas.drawText(line, storageBlockLeft, lineY, storagePaint)
            lineY += storagePaint.textSize * 1.35f
        }

        // --- Manufacturer (bottom, small font) ---
        val manufacturerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = heightPx * 0.055f
        }
        canvas.drawText(
            product.manufacturer,
            margin,
            bottomAreaTop + manufacturerPaint.textSize,
            manufacturerPaint
        )

        return bitmap
    }

    private fun buildAutoSizedLayout(
        text: String,
        maxWidthPx: Int,
        maxHeightPx: Int,
        startTextSizePx: Float,
        minTextSizePx: Float,
        bold: Boolean
    ): StaticLayout {
        var textSize = startTextSizePx
        var layout: StaticLayout

        do {
            val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                this.textSize = textSize
                isFakeBoldText = bold
                color = Color.BLACK
            }
            layout = StaticLayout.Builder
                .obtain(text, 0, text.length, paint, maxWidthPx.coerceAtLeast(1))
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1f)
                .build()

            if (layout.height <= maxHeightPx || textSize <= minTextSizePx) break
            textSize -= 1f
        } while (textSize > minTextSizePx)

        return layout
    }
}
