package com.kozvits.skladid.presentation.labelpreview

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kozvits.skladid.R
import com.kozvits.skladid.domain.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelPreviewScreen(
    onPrintSuccess: () -> Unit,
    product: Product? = null,
    productId: Long? = null,
    viewModel: LabelPreviewViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(product, productId) {
        when {
            product != null -> viewModel.loadPreview(product)
            productId != null -> viewModel.loadPreviewById(productId)
        }
    }

    LaunchedEffect(state.printStatus) {
        if (state.printStatus == PrintStatus.SUCCESS) onPrintSuccess()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.label_preview_title)) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val bitmap = state.bitmap
            if (bitmap == null && state.errorMessage != null && state.product == null) {
                Text(state.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
                return@Column
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(bitmap = bitmap.asImageBitmap(), contentDescription = null)
                } else {
                    CircularProgressIndicator()
                }
            }

            state.product?.let { current ->
                var name by remember(current.id) { mutableStateOf(current.name) }
                var manufacturer by remember(current.id) { mutableStateOf(current.manufacturer) }

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        viewModel.applyTextEdit(it, manufacturer)
                    },
                    label = { Text(stringResource(R.string.recognition_field_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = manufacturer,
                    onValueChange = {
                        manufacturer = it
                        viewModel.applyTextEdit(name, it)
                    },
                    label = { Text(stringResource(R.string.recognition_field_manufacturer)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            when (state.printStatus) {
                PrintStatus.ERROR -> Text(
                    state.errorMessage ?: stringResource(R.string.label_print_error),
                    color = MaterialTheme.colorScheme.error
                )
                PrintStatus.SUCCESS -> Text(stringResource(R.string.label_print_success))
                else -> Unit
            }

            Button(
                onClick = viewModel::printAndSave,
                enabled = state.bitmap != null && state.printStatus != PrintStatus.PRINTING,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (state.printStatus == PrintStatus.PRINTING) {
                        stringResource(R.string.label_printing)
                    } else {
                        stringResource(R.string.label_print)
                    }
                )
            }
        }
    }
}
