package com.kozvits.skladid.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val manufacturer: String,
    val category: String,
    val specs: String,
    val barcode: String?,
    val recognizedText: String?,
    val itemPhotoPath: String?,
    val tagPhotoPath: String?,
    val warehouse: String,
    val rack: String,
    val shelf: String,
    val cell: String,
    val createdAtEpochMillis: Long
)
