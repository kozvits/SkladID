package com.kozvits.skladid.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.kozvits.skladid.R
import com.kozvits.skladid.domain.model.QuantityUnit

/**
 * Numeric quantity field plus a dropdown for the fixed set of measurement units
 * (шт., компл., пар., дес., л., кг.).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantityUnitPicker(
    quantityText: String,
    onQuantityTextChange: (String) -> Unit,
    unit: QuantityUnit,
    onUnitChange: (QuantityUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = quantityText,
            onValueChange = onQuantityTextChange,
            label = { Text(stringResource(R.string.recognition_field_quantity)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
        )

        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = unit.displayLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.recognition_field_unit)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.exposedDropdownSize()
            ) {
                QuantityUnit.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.displayLabel) },
                        onClick = {
                            onUnitChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/** Formats a quantity without a trailing ".0" for whole numbers, e.g. 2.0 -> "2", 2.5 -> "2.5". */
fun formatQuantity(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
