package com.kozvits.skladid.domain.usecase.product

import com.kozvits.skladid.domain.model.Product
import com.kozvits.skladid.domain.repository.ProductRepository
import javax.inject.Inject

class GetProductByIdUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(id: Long): Product? = productRepository.getById(id)
}
