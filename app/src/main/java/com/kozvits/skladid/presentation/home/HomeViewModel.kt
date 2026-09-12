package com.kozvits.skladid.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kozvits.skladid.domain.model.Product
import com.kozvits.skladid.domain.usecase.product.DeleteProductUseCase
import com.kozvits.skladid.domain.usecase.product.ObserveRecentProductsUseCase
import com.kozvits.skladid.domain.usecase.telegram.SendProductsToTelegramUseCase
import com.kozvits.skladid.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TelegramSendStatus { IDLE, SENDING, SUCCESS, ERROR }

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeRecentProducts: ObserveRecentProductsUseCase,
    private val deleteProduct: DeleteProductUseCase,
    private val sendProductsToTelegram: SendProductsToTelegramUseCase
) : ViewModel() {

    private val productsFlow = observeRecentProducts()

    val uiState: StateFlow<UiState<List<Product>>> = productsFlow
        .map<List<Product>, UiState<List<Product>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Не удалось загрузить список товаров")) }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), UiState.Loading)

    private val _telegramStatus = MutableStateFlow(TelegramSendStatus.IDLE)
    val telegramStatus: StateFlow<TelegramSendStatus> = _telegramStatus

    private val _telegramError = MutableStateFlow<String?>(null)
    val telegramError: StateFlow<String?> = _telegramError

    fun onDelete(id: Long) {
        viewModelScope.launch { deleteProduct(id) }
    }

    fun sendToTelegram() {
        viewModelScope.launch {
            _telegramStatus.value = TelegramSendStatus.SENDING
            _telegramError.value = null

            val products = productsFlow.first()
            sendProductsToTelegram(products)
                .onSuccess { _telegramStatus.value = TelegramSendStatus.SUCCESS }
                .onFailure { error ->
                    _telegramStatus.value = TelegramSendStatus.ERROR
                    _telegramError.value = error.message ?: "Ошибка отправки в Telegram"
                }
        }
    }

    fun dismissTelegramStatus() {
        _telegramStatus.value = TelegramSendStatus.IDLE
    }
}
