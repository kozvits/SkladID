package com.kozvits.skladid.presentation.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kozvits.skladid.R
import com.kozvits.skladid.domain.model.Product
import com.kozvits.skladid.presentation.common.ErrorState
import com.kozvits.skladid.presentation.common.FullScreenLoading
import com.kozvits.skladid.presentation.common.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddProduct: () -> Unit,
    onOpenSettings: () -> Unit,
    onPrintLabel: (productId: Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.nav_settings))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProduct) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.home_fab_add))
            }
        }
    ) { padding ->
        when (val s = state) {
            is UiState.Loading -> FullScreenLoading(modifier = Modifier.padding(padding))
            is UiState.Error -> ErrorState(message = s.message, modifier = Modifier.padding(padding))
            is UiState.Success -> ProductList(
                products = s.data,
                onDelete = viewModel::onDelete,
                onPrintLabel = onPrintLabel,
                paddingValues = padding
            )
        }
    }
}

@Composable
private fun ProductList(
    products: List<Product>,
    onDelete: (Long) -> Unit,
    onPrintLabel: (Long) -> Unit,
    paddingValues: PaddingValues
) {
    if (products.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.home_empty), style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(products, key = { it.id }) { product ->
            ProductRow(
                product = product,
                onDelete = { onDelete(product.id) },
                onPrintLabel = { onPrintLabel(product.id) }
            )
        }
    }
}

@Composable
private fun ProductRow(product: Product, onDelete: () -> Unit, onPrintLabel: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(product.name, style = MaterialTheme.typography.titleMedium)
                    Text(product.manufacturer, style = MaterialTheme.typography.bodyMedium)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null
                    )
                }
            }

            if (expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LabeledValue(stringResource(R.string.recognition_field_name), product.name)
                    LabeledValue(stringResource(R.string.recognition_field_manufacturer), product.manufacturer)
                    LabeledValue(stringResource(R.string.recognition_field_category), product.category)
                    LabeledValue(stringResource(R.string.recognition_field_specs), product.specs)
                    LabeledValue(
                        stringResource(R.string.recognition_field_barcode),
                        product.barcode.orEmpty()
                    )

                    Button(
                        onClick = onPrintLabel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(stringResource(R.string.home_print_label))
                    }
                }
            } else {
                val hasAddress = listOf(
                    product.storageAddress.warehouse,
                    product.storageAddress.rack,
                    product.storageAddress.shelf,
                    product.storageAddress.cell
                ).any { it.isNotBlank() }

                if (hasAddress) {
                    Text(
                        "${product.storageAddress.warehouse} / ${product.storageAddress.rack} / " +
                            "${product.storageAddress.shelf} / ${product.storageAddress.cell}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun LabeledValue(label: String, value: String) {
    Text(
        "$label: ${value.ifBlank { "—" }}",
        style = MaterialTheme.typography.bodyMedium
    )
}
