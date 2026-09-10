package com.kozvits.skladid.data.repository

import android.content.Context
import android.net.Uri
import com.kozvits.skladid.data.warehouse.WarehouseJsonParser
import com.kozvits.skladid.domain.model.WarehouseTree
import com.kozvits.skladid.domain.repository.WarehouseRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private const val CACHE_FILE_NAME = "warehouse_tree_cache.json"

@Singleton
class WarehouseRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val parser: WarehouseJsonParser
) : WarehouseRepository {

    private val treeState = MutableStateFlow(WarehouseTree.EMPTY)

    init {
        // Best-effort synchronous warm start from cache; safe since it's a small local file.
        cacheFile().takeIf { it.exists() }?.let { file ->
            runCatching { parser.parseFromString(file.readText()) }
                .onSuccess { treeState.value = it }
        }
    }

    override fun observeTree(): Flow<WarehouseTree> = treeState.asStateFlow()

    override suspend fun importFromUri(uri: Uri): Result<WarehouseTree> = withContext(Dispatchers.IO) {
        runCatching {
            val tree = parser.parseFromUri(context.contentResolver, uri)
            cacheFile().writeText(parser.serialize(tree))
            treeState.value = tree
            tree
        }
    }

    override suspend fun loadCached(): WarehouseTree = withContext(Dispatchers.IO) {
        val file = cacheFile()
        if (!file.exists()) return@withContext WarehouseTree.EMPTY
        runCatching { parser.parseFromString(file.readText()) }
            .onSuccess { treeState.value = it }
            .getOrDefault(WarehouseTree.EMPTY)
    }

    private fun cacheFile(): File = File(context.filesDir, CACHE_FILE_NAME)
}
