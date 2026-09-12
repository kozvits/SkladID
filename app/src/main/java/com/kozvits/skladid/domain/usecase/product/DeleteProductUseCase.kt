package com.kozvits.skladid.domain.usecase.product

import com.kozvits.skladid.domain.repository.ProductRepository
import javax.inject.Inject

class DeleteProductUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(id: Long) = productRepository.delete(id)
}
