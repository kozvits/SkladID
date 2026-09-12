package com.kozvits.skladid.domain.usecase.warehouse

import com.kozvits.skladid.domain.model.StorageAddress
import com.kozvits.skladid.domain.repository.ProductRepository
import com.kozvits.skladid.domain.repository.WarehouseRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SuggestStorageCellUseCase @Inject constructor(
    private val warehouseRepository: WarehouseRepository,
    private val productRepository: ProductRepository
) {
    /**
     * Returns the first free cell in the loaded warehouse tree, or null if the tree is empty
     * or fully occupied. [categoryHint] is accepted for future rule-based placement
     * (e.g. category-specific racks) but the current rule is simple first-free-cell.
     */
    suspend operator fun invoke(categoryHint: String? = null): StorageAddress? {
        val tree = warehouseRepository.observeTree().first()
        if (tree.warehouses.isEmpty()) return null
        val occupied = productRepository.getOccupiedAddresses()
        return tree.suggestCell(occupied, categoryHint)
    }
}
