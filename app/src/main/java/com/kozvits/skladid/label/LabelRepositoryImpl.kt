package com.kozvits.skladid.label

import android.graphics.Bitmap
import com.kozvits.skladid.domain.model.Product
import com.kozvits.skladid.domain.repository.LabelRepository
import com.kozvits.skladid.domain.repository.LabelSettings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LabelRepositoryImpl @Inject constructor(
    private val labelRenderer: LabelRenderer
) : LabelRepository {
    override fun generateLabel(product: Product, settings: LabelSettings): Bitmap =
        labelRenderer.render(product, settings)
}
