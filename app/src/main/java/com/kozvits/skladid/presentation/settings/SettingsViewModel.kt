package com.kozvits.skladid.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kozvits.skladid.domain.model.BluetoothPrinterInfo
import com.kozvits.skladid.domain.model.OpenRouterModelInfo
import com.kozvits.skladid.domain.repository.LabelSettings
import com.kozvits.skladid.domain.repository.PrinterConnectionType
import com.kozvits.skladid.domain.repository.PrinterSettings
import com.kozvits.skladid.domain.usecase.label.ListPairedBluetoothPrintersUseCase
import com.kozvits.skladid.domain.usecase.recognition.FetchVisionModelsUseCase
import com.kozvits.skladid.domain.usecase.warehouse.ImportWarehouseJsonUseCase
import com.kozvits.skladid.domain.usecase.settings.GetApiKeyUseCase
import com.kozvits.skladid.domain.usecase.settings.ObserveLabelSettingsUseCase
import com.kozvits.skladid.domain.usecase.settings.ObservePrinterSettingsUseCase
import com.kozvits.skladid.domain.usecase.settings.ObserveSelectedModelIdUseCase
import com.kozvits.skladid.domain.usecase.settings.SetApiKeyUseCase
import com.kozvits.skladid.domain.usecase.settings.SetLabelSettingsUseCase
import com.kozvits.skladid.domain.usecase.settings.SetPrinterSettingsUseCase
import com.kozvits.skladid.domain.usecase.settings.SetSelectedModelIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val apiKey: String = "",
    val selectedModelId: String? = null,
    val availableModels: List<OpenRouterModelInfo> = emptyList(),
    val isRefreshingModels: Boolean = false,
    val modelsError: String? = null,
    val printerSettings: PrinterSettings = PrinterSettings(),
    val pairedBluetoothDevices: List<BluetoothPrinterInfo> = emptyList(),
    val labelSettings: LabelSettings = LabelSettings(),
    val savedMessageVisible: Boolean = false,
    val importMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getApiKey: GetApiKeyUseCase,
    private val setApiKey: SetApiKeyUseCase,
    observeSelectedModelId: ObserveSelectedModelIdUseCase,
    private val setSelectedModelId: SetSelectedModelIdUseCase,
    private val fetchVisionModels: FetchVisionModelsUseCase,
    observePrinterSettings: ObservePrinterSettingsUseCase,
    private val setPrinterSettings: SetPrinterSettingsUseCase,
    private val listPairedBluetoothPrinters: ListPairedBluetoothPrintersUseCase,
    observeLabelSettings: ObserveLabelSettingsUseCase,
    private val setLabelSettings: SetLabelSettingsUseCase,
    private val importWarehouseJsonUseCase: ImportWarehouseJsonUseCase
) : ViewModel() {

    private val apiKeyState = MutableStateFlow("")
    private val availableModels = MutableStateFlow<List<OpenRouterModelInfo>>(emptyList())
    private val isRefreshingModels = MutableStateFlow(false)
    private val modelsError = MutableStateFlow<String?>(null)
    private val pairedDevices = MutableStateFlow<List<BluetoothPrinterInfo>>(emptyList())
    private val savedMessageVisible = MutableStateFlow(false)
    private val importMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        apiKeyState,
        observeSelectedModelId(),
        availableModels,
        isRefreshingModels,
        modelsError,
        observePrinterSettings(),
        pairedDevices,
        observeLabelSettings(),
        savedMessageVisible,
        importMessage
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        SettingsUiState(
            apiKey = values[0] as String,
            selectedModelId = values[1] as String?,
            availableModels = values[2] as List<OpenRouterModelInfo>,
            isRefreshingModels = values[3] as Boolean,
            modelsError = values[4] as String?,
            printerSettings = values[5] as PrinterSettings,
            pairedBluetoothDevices = values[6] as List<BluetoothPrinterInfo>,
            labelSettings = values[7] as LabelSettings,
            savedMessageVisible = values[8] as Boolean,
            importMessage = values[9] as String?
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    init {
        viewModelScope.launch { apiKeyState.value = getApiKey().orEmpty() }
        refreshPairedBluetoothDevices()
    }

    fun onApiKeyChanged(value: String) {
        apiKeyState.value = value
    }

    fun saveApiKey() {
        viewModelScope.launch {
            setApiKey(apiKeyState.value)
            flashSaved()
        }
    }

    fun refreshModels() {
        viewModelScope.launch {
            isRefreshingModels.value = true
            modelsError.value = null
            fetchVisionModels()
                .onSuccess { availableModels.value = it }
                .onFailure { modelsError.value = it.message ?: "Не удалось получить список моделей" }
            isRefreshingModels.value = false
        }
    }

    fun selectModel(modelId: String) {
        viewModelScope.launch { setSelectedModelId(modelId) }
    }

    fun refreshPairedBluetoothDevices() {
        listPairedBluetoothPrinters()
            .onSuccess { pairedDevices.value = it }
            .onFailure { pairedDevices.value = emptyList() }
    }

    fun updatePrinterSettings(settings: PrinterSettings) {
        viewModelScope.launch {
            setPrinterSettings(settings)
            flashSaved()
        }
    }

    fun updateLabelSettings(settings: LabelSettings) {
        viewModelScope.launch {
            setLabelSettings(settings)
            flashSaved()
        }
    }

    fun importWarehouseJson(uri: android.net.Uri) {
        viewModelScope.launch {
            importWarehouseJsonUseCase(uri)
                .onSuccess { importMessage.value = "Импортировано" }
                .onFailure { importMessage.value = it.message ?: "Ошибка импорта" }
            kotlinx.coroutines.delay(2000)
            importMessage.value = null
        }
    }

    private suspend fun flashSaved() {
        savedMessageVisible.value = true
        kotlinx.coroutines.delay(1500)
        savedMessageVisible.value = false
    }
}
