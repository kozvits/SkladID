package com.kozvits.skladid.domain.model

data class Product(
    val id: Long = 0L,
    val name: String,
    val manufacturer: String,
    val category: String,
    val specs: String,
    val applicability: String = "",
    val barcode: String? = null,
    val recognizedText: String? = null,
    val itemPhotoPath: String? = null,
    val tagPhotoPath: String? = null,
    val quantity: Double = 1.0,
    val unit: QuantityUnit = QuantityUnit.DEFAULT,
    val storageAddress: StorageAddress,
    val createdAtEpochMillis: Long
)
