package com.kozvits.skladid.data

data class ProductItem(
    val name: String,
    val warehouse: String,
    val rack: String,
    val shelf: String,
    val cell: String,
    val barcode: String,
    val manufacturer: String
)
