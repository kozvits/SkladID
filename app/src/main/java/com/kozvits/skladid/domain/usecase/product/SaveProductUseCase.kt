package com.kozvits.skladid.domain.usecase.product

import com.kozvits.skladid.domain.model.Product
import com.kozvits.skladid.domain.repository.ProductRepository
import javax.inject.Inject

class SaveProductUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    sealed interface Result {
        data class Success(val id: Long) : Result
        data class ValidationError(val message: String) : Result
    }

    suspend operator fun invoke(product: Product): Result {
        if (product.name.isBlank()) {
            return Result.ValidationError("Укажите название товара")
        }
        if (!product.storageAddress.isComplete()) {
            return Result.ValidationError("Выберите полный адрес хранения")
        }
        val id = productRepository.save(product)
        return Result.Success(id)
    }
}
