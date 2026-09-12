package com.kozvits.skladid.telegram

import kotlinx.serialization.Serializable

@Serializable
data class ProductExportFileDto(
    val exportedAtEpochMillis: Long,
    val products: List<ProductExportDto>
)

@Serializable
data class ProductExportDto(
    val id: Long,
    val name: String,
    val manufacturer: String,
    val category: String,
    val specs: String,
    val barcode: String? = null,
    val warehouse: String,
    val rack: String,
    val shelf: String,
    val cell: String,
    val createdAtEpochMillis: Long
)
