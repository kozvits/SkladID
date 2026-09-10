package com.kozvits.skladid.domain.repository

import com.kozvits.skladid.domain.model.Product
import com.kozvits.skladid.domain.model.StorageAddress
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeRecentProducts(limit: Int = 100): Flow<List<Product>>
    suspend fun getById(id: Long): Product?
    suspend fun save(product: Product): Long
    suspend fun delete(id: Long)
    suspend fun getOccupiedAddresses(): Set<StorageAddress>
}
