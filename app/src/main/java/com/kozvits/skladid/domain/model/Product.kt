package com.kozvits.skladid.domain.model

data class Product(
    val id: Long = 0L,
    val name: String,
    val manufacturer: String,
    val category: String,
    val specs: String,
    val barcode: String? = null,
    val recognizedText: String? = null,
    val itemPhotoPath: String? = null,
    val tagPhotoPath: String? = null,
    val storageAddress: StorageAddress,
    val createdAtEpochMillis: Long
)
