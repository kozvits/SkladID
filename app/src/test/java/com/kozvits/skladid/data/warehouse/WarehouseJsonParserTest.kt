package com.kozvits.skladid.data.warehouse

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WarehouseJsonParserTest {

    private val parser = WarehouseJsonParser()

    private val validJson = """
        {
          "warehouses": [
            {
              "name": "Склад 1",
              "racks": [
                {
                  "name": "Стеллаж A",
                  "shelves": [
                    {
                      "name": "Полка 1",
                      "cells": ["Ячейка 1", "Ячейка 2", "Ячейка 3"]
                    }
                  ]
                }
              ]
            }
          ]
        }
    """.trimIndent()

    @Test
    fun `parses valid JSON into the expected tree`() {
        val tree = parser.parseFromString(validJson)

        assertThat(tree.warehouses).hasSize(1)
        val warehouse = tree.warehouses.first()
        assertThat(warehouse.name).isEqualTo("Склад 1")
        assertThat(warehouse.racks).hasSize(1)

        val rack = warehouse.racks.first()
        assertThat(rack.name).isEqualTo("Стеллаж A")
        assertThat(rack.shelves).hasSize(1)

        val shelf = rack.shelves.first()
        assertThat(shelf.name).isEqualTo("Полка 1")
        assertThat(shelf.cells).containsExactly("Ячейка 1", "Ячейка 2", "Ячейка 3").inOrder()
    }

    @Test
    fun `throws a descriptive error for malformed JSON`() {
        val exception = org.junit.Assert.assertThrows(WarehouseJsonParseException::class.java) {
            parser.parseFromString("{ not valid json")
        }
        assertThat(exception.message).contains("Некорректный формат JSON")
    }

    @Test
    fun `throws when the warehouses list is empty`() {
        val exception = org.junit.Assert.assertThrows(WarehouseJsonParseException::class.java) {
            parser.parseFromString("""{ "warehouses": [] }""")
        }
        assertThat(exception.message).contains("нет ни одного склада")
    }

    @Test
    fun `throws when a warehouse name is blank`() {
        val json = """{ "warehouses": [ { "name": "", "racks": [] } ] }"""
        val exception = org.junit.Assert.assertThrows(WarehouseJsonParseException::class.java) {
            parser.parseFromString(json)
        }
        assertThat(exception.message).contains("отсутствует имя")
    }

    @Test
    fun `throws when a rack name is blank`() {
        val json = """
            { "warehouses": [ { "name": "Склад 1", "racks": [ { "name": "", "shelves": [] } ] } ] }
        """.trimIndent()
        val exception = org.junit.Assert.assertThrows(WarehouseJsonParseException::class.java) {
            parser.parseFromString(json)
        }
        assertThat(exception.message).contains("стеллажа")
    }

    @Test
    fun `throws when a shelf name is blank`() {
        val json = """
            {
              "warehouses": [
                {
                  "name": "Склад 1",
                  "racks": [
                    { "name": "Стеллаж A", "shelves": [ { "name": "", "cells": [] } ] }
                  ]
                }
              ]
            }
        """.trimIndent()
        val exception = org.junit.Assert.assertThrows(WarehouseJsonParseException::class.java) {
            parser.parseFromString(json)
        }
        assertThat(exception.message).contains("полки")
    }

    @Test
    fun `tolerates missing optional arrays and unknown fields`() {
        val json = """
            {
              "warehouses": [ { "name": "Склад 1", "somethingElse": 42 } ],
              "extraTopLevelField": true
            }
        """.trimIndent()

        val tree = parser.parseFromString(json)

        assertThat(tree.warehouses).hasSize(1)
        assertThat(tree.warehouses.first().racks).isEmpty()
    }

    @Test
    fun `serialize then parseFromString round-trips to an equal tree`() {
        val original = parser.parseFromString(validJson)
        val serialized = parser.serialize(original)
        val roundTripped = parser.parseFromString(serialized)

        assertThat(roundTripped).isEqualTo(original)
    }
}
