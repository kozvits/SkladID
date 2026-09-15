package com.kozvits.skladid.domain.model

enum class QuantityUnit(val displayLabel: String) {
    PIECE("шт."),
    SET("компл."),
    PAIR("пар."),
    DOZEN("дес."),
    LITER("л."),
    KILOGRAM("кг.");

    companion object {
        val DEFAULT = PIECE

        fun fromDisplayLabel(label: String): QuantityUnit =
            entries.firstOrNull { it.displayLabel == label } ?: DEFAULT

        fun fromStorageName(name: String?): QuantityUnit =
            name?.let { stored -> entries.firstOrNull { it.name == stored } } ?: DEFAULT
    }
}
