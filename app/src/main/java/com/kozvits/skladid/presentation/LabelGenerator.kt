package com.kozvits.skladid.presentation

import android.graphics.*
import com.kozvits.skladid.data.ProductItem

fun generateLabelImage(item: ProductItem): Bitmap {
    val w = 600
    val h = 300
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = Color.WHITE
    canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
    paint.color = Color.BLACK
    paint.textSize = 28f
    canvas.drawText(item.name, 20f, 60f, paint)
    // barcode placeholder
    paint.textSize = 22f
    canvas.drawText("1-111100-001336", 20f, 120f, paint)
    paint.textSize = 18f
    canvas.drawText("Склад: ${item.warehouse} Стеллаж: ${item.rack} Полка: ${item.shelf} Ячейка: ${item.cell}", 20f, 180f, paint)
    canvas.drawText("Производитель: ${item.manufacturer}", 20f, 240f, paint)
    return bmp
}
