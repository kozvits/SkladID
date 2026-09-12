package com.kozvits.skladid.domain.model

/**
 * In-memory representation of the warehouse placement tree imported from JSON.
 * Mirrors the structure: Склад → Стеллаж → Полка → Ячейка.
 */
data class WarehouseTree(
    val warehouses: List<Warehouse>
) {
    fun findCell(address: StorageAddress): Boolean =
        warehouses.any { wh ->
            wh.name == address.warehouse && wh.racks.any { rack ->
                rack.name == address.rack && rack.shelves.any { shelf ->
                    shelf.name == address.shelf && shelf.cells.contains(address.cell)
                }
            }
        }

    /** Returns the first empty/free cell, optionally filtered to a category hint. */
    fun suggestCell(occupiedCells: Set<StorageAddress>, categoryHint: String? = null): StorageAddress? {
        for (wh in warehouses) {
            for (rack in wh.racks) {
                for (shelf in rack.shelves) {
                    for (cell in shelf.cells) {
                        val candidate = StorageAddress(wh.name, rack.name, shelf.name, cell)
                        if (candidate !in occupiedCells) {
                            return candidate
                        }
                    }
                }
            }
        }
        return null
    }

    companion object {
        val EMPTY = WarehouseTree(emptyList())
    }
}

data class Warehouse(
    val name: String,
    val racks: List<Rack>
)

data class Rack(
    val name: String,
    val shelves: List<Shelf>
)

data class Shelf(
    val name: String,
    val cells: List<String>
)
