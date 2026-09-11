package com.kozvits.skladid.presentation.storage

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kozvits.skladid.R
import com.kozvits.skladid.domain.model.WarehouseTree

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageScreen(
    onConfirmed: (warehouse: String, rack: String, shelf: String, cell: String) -> Unit,
    viewModel: StorageViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(viewModel::importJson) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.storage_title)) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.tree.warehouses.isEmpty()) {
                Text(stringResource(R.string.storage_no_db), style = MaterialTheme.typography.bodyLarge)
                Button(
                    onClick = { importLauncher.launch(arrayOf("application/json")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.storage_import))
                }
                state.importError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                return@Scaffold
            }

            val tree = state.tree
            val sel = state.selection

            val warehouseNames = tree.warehouses.map { it.name }
            val racks = tree.warehouses.firstOrNull { it.name == sel.warehouse }?.racks.orEmpty()
            val shelves = racks.firstOrNull { it.name == sel.rack }?.shelves.orEmpty()
            val cells = shelves.firstOrNull { it.name == sel.shelf }?.cells.orEmpty()

            LabeledDropdown(
                label = stringResource(R.string.storage_warehouse),
                options = warehouseNames,
                selected = sel.warehouse,
                onSelected = viewModel::selectWarehouse
            )
            LabeledDropdown(
                label = stringResource(R.string.storage_rack),
                options = racks.map { it.name },
                selected = sel.rack,
                onSelected = viewModel::selectRack,
                enabled = sel.warehouse != null
            )
            LabeledDropdown(
                label = stringResource(R.string.storage_shelf),
                options = shelves.map { it.name },
                selected = sel.shelf,
                onSelected = viewModel::selectShelf,
                enabled = sel.rack != null
            )
            LabeledDropdown(
                label = stringResource(R.string.storage_cell),
                options = cells,
                selected = sel.cell,
                onSelected = viewModel::selectCell,
                enabled = sel.shelf != null
            )

            OutlinedButton(
                onClick = { viewModel.autoSuggest() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.storage_auto_suggest))
            }

            Button(
                onClick = {
                    sel.toAddressOrNull()?.let {
                        onConfirmed(it.warehouse, it.rack, it.shelf, it.cell)
                    }
                },
                enabled = sel.toAddressOrNull() != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.common_next))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LabeledDropdown(
    label: String,
    options: List<String>,
    selected: String?,
    onSelected: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        OutlinedTextField(
            value = selected.orEmpty(),
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        DropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false },
            modifier = Modifier.exposedDropdownSize()
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
