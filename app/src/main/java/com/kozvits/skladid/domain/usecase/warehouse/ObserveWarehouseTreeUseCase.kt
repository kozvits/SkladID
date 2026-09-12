package com.kozvits.skladid.domain.usecase.warehouse

import com.kozvits.skladid.domain.model.WarehouseTree
import com.kozvits.skladid.domain.repository.WarehouseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveWarehouseTreeUseCase @Inject constructor(
    private val warehouseRepository: WarehouseRepository
) {
    operator fun invoke(): Flow<WarehouseTree> = warehouseRepository.observeTree()
}
