package com.kozvits.skladid.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kozvits.skladid.domain.model.Product
import com.kozvits.skladid.domain.usecase.product.DeleteProductUseCase
import com.kozvits.skladid.domain.usecase.product.ObserveRecentProductsUseCase
import com.kozvits.skladid.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeRecentProducts: ObserveRecentProductsUseCase,
    private val deleteProduct: DeleteProductUseCase
) : ViewModel() {

    val uiState: StateFlow<UiState<List<Product>>> = observeRecentProducts()
        .map<List<Product>, UiState<List<Product>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Не удалось загрузить список товаров")) }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), UiState.Loading)

    fun onDelete(id: Long) {
        viewModelScope.launch { deleteProduct(id) }
    }
}
