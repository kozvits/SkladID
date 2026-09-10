package com.kozvits.skladid.data.local.db.entity

import com.kozvits.skladid.domain.model.Product
import com.kozvits.skladid.domain.model.StorageAddress

fun ProductEntity.toDomain(): Product = Product(
    id = id,
    name = name,
    manufacturer = manufacturer,
    category = category,
    specs = specs,
    barcode = barcode,
    recognizedText = recognizedText,
    itemPhotoPath = itemPhotoPath,
    tagPhotoPath = tagPhotoPath,
    storageAddress = StorageAddress(warehouse, rack, shelf, cell),
    createdAtEpochMillis = createdAtEpochMillis
)

fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    name = name,
    manufacturer = manufacturer,
    category = category,
    specs = specs,
    barcode = barcode,
    recognizedText = recognizedText,
    itemPhotoPath = itemPhotoPath,
    tagPhotoPath = tagPhotoPath,
    warehouse = storageAddress.warehouse,
    rack = storageAddress.rack,
    shelf = storageAddress.shelf,
    cell = storageAddress.cell,
    createdAtEpochMillis = createdAtEpochMillis
)
