package com.kozvits.skladid.presentation.storage

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kozvits.skladid.domain.model.StorageAddress
import com.kozvits.skladid.domain.model.WarehouseTree
import com.kozvits.skladid.domain.usecase.warehouse.ImportWarehouseJsonUseCase
import com.kozvits.skladid.domain.usecase.warehouse.ObserveWarehouseTreeUseCase
import com.kozvits.skladid.domain.usecase.warehouse.SuggestStorageCellUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StorageSelection(
    val warehouse: String? = null,
    val rack: String? = null,
    val shelf: String? = null,
    val cell: String? = null
) {
    fun toAddressOrNull(): StorageAddress? =
        if (warehouse != null && rack != null && shelf != null && cell != null) {
            StorageAddress(warehouse, rack, shelf, cell)
        } else {
            null
        }
}

data class StorageUiState(
    val tree: WarehouseTree = WarehouseTree.EMPTY,
    val selection: StorageSelection = StorageSelection(),
    val importError: String? = null
)

@HiltViewModel
class StorageViewModel @Inject constructor(
    observeWarehouseTree: ObserveWarehouseTreeUseCase,
    private val importWarehouseJson: ImportWarehouseJsonUseCase,
    private val suggestStorageCell: SuggestStorageCellUseCase
) : ViewModel() {

    private val selection = MutableStateFlow(StorageSelection())
    private val importError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<StorageUiState> = combine(
        observeWarehouseTree(),
        selection,
        importError
    ) { tree, sel, error ->
        StorageUiState(tree = tree, selection = sel, importError = error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StorageUiState())

    fun selectWarehouse(name: String) {
        selection.value = StorageSelection(warehouse = name)
    }

    fun selectRack(name: String) {
        selection.value = selection.value.copy(rack = name, shelf = null, cell = null)
    }

    fun selectShelf(name: String) {
        selection.value = selection.value.copy(shelf = name, cell = null)
    }

    fun selectCell(name: String) {
        selection.value = selection.value.copy(cell = name)
    }

    fun importJson(uri: Uri) {
        viewModelScope.launch {
            importWarehouseJson(uri)
                .onFailure { importError.value = it.message ?: "Ошибка импорта" }
                .onSuccess { importError.value = null }
        }
    }

    fun autoSuggest(categoryHint: String? = null) {
        viewModelScope.launch {
            suggestStorageCell(categoryHint)?.let { address ->
                selection.value = StorageSelection(
                    warehouse = address.warehouse,
                    rack = address.rack,
                    shelf = address.shelf,
                    cell = address.cell
                )
            }
        }
    }
}
