package com.kozvits.skladid.data.repository

import com.kozvits.skladid.data.local.db.dao.ProductDao
import com.kozvits.skladid.data.local.db.entity.toDomain
import com.kozvits.skladid.data.local.db.entity.toEntity
import com.kozvits.skladid.domain.model.Product
import com.kozvits.skladid.domain.model.StorageAddress
import com.kozvits.skladid.domain.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao
) : ProductRepository {

    override fun observeRecentProducts(limit: Int): Flow<List<Product>> =
        productDao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Product? = withContext(Dispatchers.IO) {
        productDao.getById(id)?.toDomain()
    }

    override suspend fun save(product: Product): Long = withContext(Dispatchers.IO) {
        productDao.upsert(product.toEntity())
    }

    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        productDao.deleteById(id)
    }

    override suspend fun getOccupiedAddresses(): Set<StorageAddress> = withContext(Dispatchers.IO) {
        productDao.getAllOccupiedCells()
            .map { StorageAddress(it.warehouse, it.rack, it.shelf, it.cell) }
            .toSet()
    }
}
