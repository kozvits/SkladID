package com.kozvits.skladid.telegram

import com.kozvits.skladid.domain.model.Product
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private const val TELEGRAM_MESSAGE_LIMIT = 3500 // Telegram's hard cap is 4096; leave headroom.

@Singleton
class ProductExportFormatter @Inject constructor() {

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    fun toJson(products: List<Product>): String {
        val dto = ProductExportFileDto(
            exportedAtEpochMillis = System.currentTimeMillis(),
            products = products.map { p ->
                ProductExportDto(
                    id = p.id,
                    name = p.name,
                    manufacturer = p.manufacturer,
                    category = p.category,
                    specs = p.specs,
                    applicability = p.applicability,
                    barcode = p.barcode,
                    quantity = p.quantity,
                    unit = p.unit.name,
                    warehouse = p.storageAddress.warehouse,
                    rack = p.storageAddress.rack,
                    shelf = p.storageAddress.shelf,
                    cell = p.storageAddress.cell,
                    createdAtEpochMillis = p.createdAtEpochMillis
                )
            }
        )
        return json.encodeToString(ProductExportFileDto.serializer(), dto)
    }

    fun toTextSummary(products: List<Product>): String {
        if (products.isEmpty()) return "Список товаров пуст."

        return buildString {
            appendLine("Список товаров (${products.size}):")
            products.forEachIndexed { index, p ->
                appendLine()
                appendLine("${index + 1}. ${p.name.ifBlank { "Без названия" }}")
                if (p.manufacturer.isNotBlank()) appendLine("Производитель: ${p.manufacturer}")
                if (p.category.isNotBlank()) appendLine("Категория: ${p.category}")
                if (p.specs.isNotBlank()) appendLine("Характеристики: ${p.specs}")
                if (p.applicability.isNotBlank()) appendLine("Применимость: ${p.applicability}")
                if (!p.barcode.isNullOrBlank()) appendLine("Штрих-код: ${p.barcode}")
                appendLine("Количество: ${formatQuantity(p.quantity)} ${p.unit.displayLabel}")

                val address = p.storageAddress
                val addressParts = buildList {
                    if (address.warehouse.isNotBlank()) add("Склад ${address.warehouse}")
                    if (address.rack.isNotBlank()) add("Стеллаж ${address.rack}")
                    if (address.shelf.isNotBlank()) add("Полка ${address.shelf}")
                    if (address.cell.isNotBlank()) add("Ячейка ${address.cell}")
                }
                if (addressParts.isNotEmpty()) {
                    appendLine("Адрес: ${addressParts.joinToString(", ")}")
                }
            }
        }.trim()
    }

    /** Telegram caps sendMessage text at 4096 characters — split long summaries on line breaks. */
    fun splitForTelegram(text: String, maxLength: Int = TELEGRAM_MESSAGE_LIMIT): List<String> {
        if (text.length <= maxLength) return listOf(text)

        val chunks = mutableListOf<String>()
        val current = StringBuilder()
        for (line in text.lineSequence()) {
            if (current.length + line.length + 1 > maxLength && current.isNotEmpty()) {
                chunks += current.toString().trim()
                current.clear()
            }
            current.appendLine(line)
        }
        if (current.isNotEmpty()) chunks += current.toString().trim()
        return chunks
    }

    /** Formats a quantity without a trailing ".0" for whole numbers, e.g. 2.0 -> "2", 2.5 -> "2.5". */
    private fun formatQuantity(value: Double): String =
        if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
}
