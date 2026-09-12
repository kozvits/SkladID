package com.kozvits.skladid.presentation.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kozvits.skladid.R
import com.kozvits.skladid.domain.repository.PrinterConnectionType
import com.kozvits.skladid.domain.repository.PrinterSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(viewModel::importWarehouseJson) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // API key
            var apiKeyFieldValue by remember(state.apiKey) { mutableStateOf(state.apiKey) }
            OutlinedTextField(
                value = apiKeyFieldValue,
                onValueChange = { apiKeyFieldValue = it; viewModel.onApiKeyChanged(it) },
                label = { Text(stringResource(R.string.settings_api_key)) },
                placeholder = { Text(stringResource(R.string.settings_api_key_hint)) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = viewModel::saveApiKey, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.common_save))
            }

            HorizontalDivider()

            // Model picker
            Text(stringResource(R.string.settings_model), style = MaterialTheme.typography.titleMedium)
            ModelDropdown(
                models = state.availableModels,
                selectedId = state.selectedModelId,
                onSelected = viewModel::selectModel
            )
            OutlinedButton(
                onClick = viewModel::refreshModels,
                enabled = !state.isRefreshingModels,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.settings_refresh_models))
            }
            state.modelsError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            HorizontalDivider()

            // Warehouse JSON import
            Button(
                onClick = { importLauncher.launch(arrayOf("application/json")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.settings_import_json))
            }

            HorizontalDivider()

            // Printer settings
            Text(stringResource(R.string.settings_printer_section), style = MaterialTheme.typography.titleMedium)
            PrinterSettingsSection(
                settings = state.printerSettings,
                onSettingsChanged = viewModel::updatePrinterSettings
            )

            HorizontalDivider()

            // Label size
            Text(stringResource(R.string.settings_label_size), style = MaterialTheme.typography.titleMedium)
            LabelSizeSection(
                widthMm = state.labelSettings.widthMm,
                heightMm = state.labelSettings.heightMm,
                dpi = state.labelSettings.dpi,
                onChanged = viewModel::updateLabelSettings
            )

            HorizontalDivider()

            // Telegram bot
            Text(stringResource(R.string.settings_telegram_section), style = MaterialTheme.typography.titleMedium)
            var botTokenFieldValue by remember(state.telegramBotToken) { mutableStateOf(state.telegramBotToken) }
            var chatIdFieldValue by remember(state.telegramChatId) { mutableStateOf(state.telegramChatId) }
            OutlinedTextField(
                value = botTokenFieldValue,
                onValueChange = { botTokenFieldValue = it; viewModel.onTelegramBotTokenChanged(it) },
                label = { Text(stringResource(R.string.settings_telegram_bot_token)) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = chatIdFieldValue,
                onValueChange = { chatIdFieldValue = it; viewModel.onTelegramChatIdChanged(it) },
                label = { Text(stringResource(R.string.settings_telegram_chat_id)) },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = viewModel::saveTelegramSettings, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.common_save))
            }

            if (state.savedMessageVisible) {
                Text(stringResource(R.string.settings_saved), color = MaterialTheme.colorScheme.primary)
            }
            state.importMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelDropdown(
    models: List<com.kozvits.skladid.domain.model.OpenRouterModelInfo>,
    selectedId: String?,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = models.firstOrNull { it.id == selectedId }?.displayName ?: selectedId.orEmpty()

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
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
            models.forEach { model ->
                DropdownMenuItem(
                    text = { Text(model.displayName) },
                    onClick = {
                        onSelected(model.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PrinterSettingsSection(
    settings: PrinterSettings,
    onSettingsChanged: (PrinterSettings) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            RadioButton(
                selected = settings.connectionType == PrinterConnectionType.BLUETOOTH,
                onClick = { onSettingsChanged(settings.copy(connectionType = PrinterConnectionType.BLUETOOTH)) }
            )
            Text(stringResource(R.string.settings_printer_bluetooth))
            RadioButton(
                selected = settings.connectionType == PrinterConnectionType.NETWORK,
                onClick = { onSettingsChanged(settings.copy(connectionType = PrinterConnectionType.NETWORK)) }
            )
            Text(stringResource(R.string.settings_printer_network))
        }

        if (settings.connectionType == PrinterConnectionType.NETWORK) {
            var ip by remember(settings.networkIp) { mutableStateOf(settings.networkIp.orEmpty()) }
            var port by remember(settings.networkPort) { mutableStateOf(settings.networkPort.toString()) }

            OutlinedTextField(
                value = ip,
                onValueChange = {
                    ip = it
                    onSettingsChanged(settings.copy(networkIp = it))
                },
                label = { Text(stringResource(R.string.settings_printer_ip)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = port,
                onValueChange = { value ->
                    port = value
                    value.toIntOrNull()?.let { onSettingsChanged(settings.copy(networkPort = it)) }
                },
                label = { Text(stringResource(R.string.settings_printer_port)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                settings.bluetoothDeviceName ?: stringResource(R.string.settings_printer_scan),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun LabelSizeSection(
    widthMm: Float,
    heightMm: Float,
    dpi: Int,
    onChanged: (com.kozvits.skladid.domain.repository.LabelSettings) -> Unit
) {
    var widthText by remember(widthMm) { mutableStateOf(widthMm.toString()) }
    var heightText by remember(heightMm) { mutableStateOf(heightMm.toString()) }
    var dpiText by remember(dpi) { mutableStateOf(dpi.toString()) }

    OutlinedTextField(
        value = widthText,
        onValueChange = { value ->
            widthText = value
            value.toFloatOrNull()?.let {
                onChanged(com.kozvits.skladid.domain.repository.LabelSettings(it, heightMm, dpi))
            }
        },
        label = { Text(stringResource(R.string.settings_label_width_mm)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = heightText,
        onValueChange = { value ->
            heightText = value
            value.toFloatOrNull()?.let {
                onChanged(com.kozvits.skladid.domain.repository.LabelSettings(widthMm, it, dpi))
            }
        },
        label = { Text(stringResource(R.string.settings_label_height_mm)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = dpiText,
        onValueChange = { value ->
            dpiText = value
            value.toIntOrNull()?.let {
                onChanged(com.kozvits.skladid.domain.repository.LabelSettings(widthMm, heightMm, it))
            }
        },
        label = { Text(stringResource(R.string.settings_label_dpi)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}
