package com.kozvits.skladid.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kozvits.skladid.domain.model.Product
import com.kozvits.skladid.domain.model.StorageAddress
import com.kozvits.skladid.domain.usecase.product.SaveProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductDraft(
    val itemPhotoPath: String? = null,
    val tagPhotoPath: String? = null,
    val name: String = "",
    val manufacturer: String = "",
    val category: String = "",
    val specs: String = "",
    val barcode: String? = null,
    val recognizedText: String? = null,
    val storageAddress: StorageAddress = StorageAddress.EMPTY
) {
    fun toProduct(): Product = Product(
        name = name,
        manufacturer = manufacturer,
        category = category,
        specs = specs,
        barcode = barcode,
        recognizedText = recognizedText,
        itemPhotoPath = itemPhotoPath,
        tagPhotoPath = tagPhotoPath,
        storageAddress = storageAddress,
        createdAtEpochMillis = System.currentTimeMillis()
    )
}

@HiltViewModel
class ProductDraftViewModel @Inject constructor(
    private val saveProduct: SaveProductUseCase
) : ViewModel() {

    private val _draft = MutableStateFlow(ProductDraft())
    val draft: StateFlow<ProductDraft> = _draft

    fun setPhotos(itemPhotoPath: String?, tagPhotoPath: String?) {
        _draft.update { it.copy(itemPhotoPath = itemPhotoPath, tagPhotoPath = tagPhotoPath) }
    }

    fun setRecognition(barcode: String?, recognizedText: String?) {
        _draft.update { it.copy(barcode = barcode, recognizedText = recognizedText) }
    }

    fun setProductFields(name: String, manufacturer: String, category: String, specs: String) {
        _draft.update {
            it.copy(name = name, manufacturer = manufacturer, category = category, specs = specs)
        }
    }

    fun setStorageAddress(address: StorageAddress) {
        _draft.update { it.copy(storageAddress = address) }
    }

    /**
     * Persists the current draft as-is (storage address may still be empty — it can be assigned
     * later, e.g. when printing the label from the saved list) and resets the draft afterwards.
     */
    fun saveDraftAsProduct(onSaved: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            when (val result = saveProduct(_draft.value.toProduct())) {
                is SaveProductUseCase.Result.Success -> {
                    reset()
                    onSaved()
                }
                is SaveProductUseCase.Result.ValidationError -> onError(result.message)
            }
        }
    }

    fun reset() {
        _draft.value = ProductDraft()
    }
}
