package com.kozvits.skladid.domain.repository

import android.net.Uri
import com.kozvits.skladid.domain.model.WarehouseTree
import kotlinx.coroutines.flow.Flow

interface WarehouseRepository {
    /** Currently loaded placement tree (empty if nothing imported yet). */
    fun observeTree(): Flow<WarehouseTree>

    /** Parses and imports a JSON file picked via SAF (ACTION_OPEN_DOCUMENT), caching it locally. */
    suspend fun importFromUri(uri: Uri): Result<WarehouseTree>

    /** Reloads the last-imported tree from local cache, if any. */
    suspend fun loadCached(): WarehouseTree
}
