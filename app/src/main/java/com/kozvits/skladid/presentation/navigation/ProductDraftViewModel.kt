package com.kozvits.skladid.presentation.navigation

import androidx.lifecycle.ViewModel
import com.kozvits.skladid.domain.model.Product
import com.kozvits.skladid.domain.model.StorageAddress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
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
class ProductDraftViewModel @Inject constructor() : ViewModel() {

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

    fun reset() {
        _draft.value = ProductDraft()
    }
}
