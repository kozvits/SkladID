package com.kozvits.skladid.presentation.labelpreview

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kozvits.skladid.domain.model.Product
import com.kozvits.skladid.domain.repository.LabelSettings
import com.kozvits.skladid.domain.repository.PrinterSettings
import com.kozvits.skladid.domain.usecase.label.GenerateLabelUseCase
import com.kozvits.skladid.domain.usecase.label.PrintLabelUseCase
import com.kozvits.skladid.domain.usecase.product.GetProductByIdUseCase
import com.kozvits.skladid.domain.usecase.product.SaveProductUseCase
import com.kozvits.skladid.domain.usecase.settings.ObserveLabelSettingsUseCase
import com.kozvits.skladid.domain.usecase.settings.ObservePrinterSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PrintStatus { IDLE, PRINTING, SUCCESS, ERROR }

data class LabelPreviewUiState(
    val product: Product? = null,
    val bitmap: Bitmap? = null,
    val printStatus: PrintStatus = PrintStatus.IDLE,
    val errorMessage: String? = null
)

@HiltViewModel
class LabelPreviewViewModel @Inject constructor(
    private val generateLabel: GenerateLabelUseCase,
    private val printLabel: PrintLabelUseCase,
    private val saveProduct: SaveProductUseCase,
    private val getProductById: GetProductByIdUseCase,
    private val observeLabelSettings: ObserveLabelSettingsUseCase,
    private val observePrinterSettings: ObservePrinterSettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LabelPreviewUiState())
    val uiState: StateFlow<LabelPreviewUiState> = _uiState

    private var currentLabelSettings: LabelSettings = LabelSettings()

    fun loadPreview(product: Product) {
        viewModelScope.launch {
            currentLabelSettings = observeLabelSettings().first()
            val bitmap = generateLabel(product, currentLabelSettings)
            _uiState.value = LabelPreviewUiState(product = product, bitmap = bitmap)
        }
    }

    /** Loads an already-saved product (e.g. printing from the Home list) before rendering it. */
    fun loadPreviewById(productId: Long) {
        viewModelScope.launch {
            val product = getProductById(productId) ?: run {
                _uiState.value = LabelPreviewUiState(errorMessage = "Товар не найден")
                return@launch
            }
            loadPreview(product)
        }
    }

    /** Regenerates the bitmap after the user tweaks name/manufacturer text in the preview. */
    fun applyTextEdit(name: String, manufacturer: String) {
        val product = _uiState.value.product?.copy(name = name, manufacturer = manufacturer) ?: return
        val bitmap = generateLabel(product, currentLabelSettings)
        _uiState.value = _uiState.value.copy(product = product, bitmap = bitmap)
    }

    fun printAndSave() {
        val product = _uiState.value.product ?: return
        val bitmap = _uiState.value.bitmap ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(printStatus = PrintStatus.PRINTING, errorMessage = null)
            val printerSettings: PrinterSettings = observePrinterSettings().first()

            printLabel(bitmap, printerSettings, currentLabelSettings)
                .onSuccess {
                    saveProduct(product)
                    _uiState.value = _uiState.value.copy(printStatus = PrintStatus.SUCCESS)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        printStatus = PrintStatus.ERROR,
                        errorMessage = error.message ?: "Ошибка печати"
                    )
                }
        }
    }
}
