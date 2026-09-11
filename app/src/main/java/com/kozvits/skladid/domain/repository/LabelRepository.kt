package com.kozvits.skladid.domain.repository

import android.graphics.Bitmap
import com.kozvits.skladid.domain.model.Product

interface LabelRepository {
    /** Renders the printable label bitmap for [product] at the given [settings]. */
    fun generateLabel(product: Product, settings: LabelSettings): Bitmap
}
