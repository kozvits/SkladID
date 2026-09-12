package com.kozvits.skladid.data.local

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val MAX_DIMENSION_PX = 1024
private const val JPEG_QUALITY = 85

@Singleton
class ImageBase64Encoder @Inject constructor() {

    suspend fun encodeDownscaled(imagePath: String): String = withContext(Dispatchers.Default) {
        val original = BitmapFactory.decodeFile(imagePath)
            ?: throw IllegalArgumentException("Не удалось загрузить изображение: $imagePath")

        val scale = MAX_DIMENSION_PX.toFloat() / maxOf(original.width, original.height)
        val scaled = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                original,
                (original.width * scale).toInt().coerceAtLeast(1),
                (original.height * scale).toInt().coerceAtLeast(1),
                true
            )
        } else {
            original
        }

        ByteArrayOutputStream().use { stream ->
            scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)
            Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
        }
    }
}
