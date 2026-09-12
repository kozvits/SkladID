package com.kozvits.skladid.data.warehouse

import kotlinx.serialization.Serializable

@Serializable
data class WarehouseFileDto(
    val warehouses: List<WarehouseDto> = emptyList()
)

@Serializable
data class WarehouseDto(
    val name: String,
    val racks: List<RackDto> = emptyList()
)

@Serializable
data class RackDto(
    val name: String,
    val shelves: List<ShelfDto> = emptyList()
)

@Serializable
data class ShelfDto(
    val name: String,
    val cells: List<String> = emptyList()
)
