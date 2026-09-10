package com.kozvits.skladid.domain.usecase.warehouse

import android.net.Uri
import com.kozvits.skladid.domain.model.WarehouseTree
import com.kozvits.skladid.domain.repository.WarehouseRepository
import javax.inject.Inject

class ImportWarehouseJsonUseCase @Inject constructor(
    private val warehouseRepository: WarehouseRepository
) {
    suspend operator fun invoke(uri: Uri): Result<WarehouseTree> =
        warehouseRepository.importFromUri(uri)
}
