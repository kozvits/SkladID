package com.kozvits.skladid.presentation.recognition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kozvits.skladid.R
import com.kozvits.skladid.presentation.common.ErrorState
import com.kozvits.skladid.presentation.common.FullScreenLoading
import com.kozvits.skladid.presentation.common.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecognitionScreen(
    itemPhotoPath: String,
    tagPhotoPath: String,
    onConfirmed: (name: String, manufacturer: String, category: String, specs: String, barcode: String?, recognizedText: String?) -> Unit,
    onSave: (name: String, manufacturer: String, category: String, specs: String, barcode: String?, recognizedText: String?) -> Unit,
    viewModel: RecognitionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(itemPhotoPath, tagPhotoPath) {
        viewModel.startRecognition(itemPhotoPath, tagPhotoPath)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.recognition_title)) }) }
    ) { padding ->
        when (val s = state) {
            is UiState.Loading -> FullScreenLoading(
                message = stringResource(R.string.recognition_loading),
                modifier = Modifier.padding(padding)
            )
            is UiState.Error -> ErrorState(
                message = s.message,
                onRetry = { viewModel.retry(itemPhotoPath, tagPhotoPath) },
                retryLabel = stringResource(R.string.recognition_retry),
                modifier = Modifier.padding(padding)
            )
            is UiState.Success -> RecognitionForm(
                initial = s.data,
                modifier = Modifier.padding(padding),
                onSave = { name, manufacturer, category, specs, barcode ->
                    onSave(name, manufacturer, category, specs, barcode, s.data.recognizedText)
                },
                onConfirm = { name, manufacturer, category, specs, barcode ->
                    onConfirmed(name, manufacturer, category, specs, barcode, s.data.recognizedText)
                }
            )
        }
    }
}

@Composable
private fun RecognitionForm(
    initial: RecognitionFields,
    modifier: Modifier = Modifier,
    onSave: (name: String, manufacturer: String, category: String, specs: String, barcode: String?) -> Unit,
    onConfirm: (name: String, manufacturer: String, category: String, specs: String, barcode: String?) -> Unit
) {
    var name by remember(initial) { mutableStateOf(initial.name) }
    var manufacturer by remember(initial) { mutableStateOf(initial.manufacturer) }
    var category by remember(initial) { mutableStateOf(initial.category) }
    var specs by remember(initial) { mutableStateOf(initial.specs) }
    var barcode by remember(initial) { mutableStateOf(initial.barcode.orEmpty()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        initial.recognizedText?.let {
            Text(
                stringResource(R.string.recognition_text_found, it),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.recognition_field_name)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = manufacturer,
            onValueChange = { manufacturer = it },
            label = { Text(stringResource(R.string.recognition_field_manufacturer)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text(stringResource(R.string.recognition_field_category)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = specs,
            onValueChange = { specs = it },
            label = { Text(stringResource(R.string.recognition_field_specs)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
        OutlinedTextField(
            value = barcode,
            onValueChange = { barcode = it },
            label = { Text(stringResource(R.string.recognition_field_barcode)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedButton(
            onClick = { onSave(name, manufacturer, category, specs, barcode.ifBlank { null }) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.common_save))
        }

        Button(
            onClick = { onConfirm(name, manufacturer, category, specs, barcode.ifBlank { null }) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.common_next))
        }
    }
}
