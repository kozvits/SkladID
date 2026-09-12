package com.kozvits.skladid.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoFileFactory @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun newPhotoFile(prefix: String): File {
        val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(System.currentTimeMillis())
        return File(imagesDir, "${prefix}_$timestamp.jpg")
    }
}
