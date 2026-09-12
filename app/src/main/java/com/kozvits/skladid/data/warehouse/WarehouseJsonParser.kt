package com.kozvits.skladid.data.warehouse

import android.content.ContentResolver
import android.net.Uri
import com.kozvits.skladid.domain.model.Rack
import com.kozvits.skladid.domain.model.Shelf
import com.kozvits.skladid.domain.model.Warehouse
import com.kozvits.skladid.domain.model.WarehouseTree
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

class WarehouseJsonParseException(message: String, cause: Throwable? = null) :
    Exception(message, cause)

@Singleton
class WarehouseJsonParser @Inject constructor() {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /** Reads the file behind [uri] via [contentResolver] and parses it into a [WarehouseTree]. */
    suspend fun parseFromUri(contentResolver: ContentResolver, uri: Uri): WarehouseTree =
        withContext(Dispatchers.IO) {
            val text = contentResolver.openInputStream(uri)?.use { it.bufferedReader().readText() }
                ?: throw WarehouseJsonParseException("Не удалось открыть файл")
            parseFromString(text)
        }

    fun parseFromString(rawJson: String): WarehouseTree {
        val dto = try {
            json.decodeFromString(WarehouseFileDto.serializer(), rawJson)
        } catch (e: Exception) {
            throw WarehouseJsonParseException("Некорректный формат JSON: ${e.message}", e)
        }

        if (dto.warehouses.isEmpty()) {
            throw WarehouseJsonParseException("В файле нет ни одного склада")
        }

        val warehouses = dto.warehouses.map { wh ->
            if (wh.name.isBlank()) throw WarehouseJsonParseException("У склада отсутствует имя")
            Warehouse(
                name = wh.name,
                racks = wh.racks.map { rack ->
                    if (rack.name.isBlank()) {
                        throw WarehouseJsonParseException("У стеллажа в '${wh.name}' отсутствует имя")
                    }
                    Rack(
                        name = rack.name,
                        shelves = rack.shelves.map { shelf ->
                            if (shelf.name.isBlank()) {
                                throw WarehouseJsonParseException(
                                    "У полки в '${wh.name} / ${rack.name}' отсутствует имя"
                                )
                            }
                            Shelf(name = shelf.name, cells = shelf.cells)
                        }
                    )
                }
            )
        }

        return WarehouseTree(warehouses)
    }

    fun serialize(tree: WarehouseTree): String {
        val dto = WarehouseFileDto(
            warehouses = tree.warehouses.map { wh ->
                WarehouseDto(
                    name = wh.name,
                    racks = wh.racks.map { rack ->
                        RackDto(
                            name = rack.name,
                            shelves = rack.shelves.map { shelf ->
                                ShelfDto(name = shelf.name, cells = shelf.cells)
                            }
                        )
                    }
                )
            }
        )
        return json.encodeToString(WarehouseFileDto.serializer(), dto)
    }
}
