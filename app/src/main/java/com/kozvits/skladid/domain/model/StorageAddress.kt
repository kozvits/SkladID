package com.kozvits.skladid.domain.model

/**
 * Address of a physical storage location: Склад → Стеллаж → Полка → Ячейка.
 */
data class StorageAddress(
    val warehouse: String,
    val rack: String,
    val shelf: String,
    val cell: String
) {
    fun isComplete(): Boolean =
        warehouse.isNotBlank() && rack.isNotBlank() && shelf.isNotBlank() && cell.isNotBlank()

    companion object {
        val EMPTY = StorageAddress("", "", "", "")
    }
}
