package com.kozvits.skladid.presentation.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun QuantityUnitPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Кол-во:")
        Button(onClick = { if (value > 1) onValueChange(value - 1) }) { Text("-") }
        Text("$value")
        Button(onClick = { onValueChange(value + 1) }) { Text("+") }
    }
}
