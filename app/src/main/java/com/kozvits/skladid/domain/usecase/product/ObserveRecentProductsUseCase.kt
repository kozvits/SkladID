package com.kozvits.skladid.domain.usecase.product

import com.kozvits.skladid.domain.model.Product
import com.kozvits.skladid.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveRecentProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    operator fun invoke(limit: Int = 100): Flow<List<Product>> =
        productRepository.observeRecentProducts(limit)
}
