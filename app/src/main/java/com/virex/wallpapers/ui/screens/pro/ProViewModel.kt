package com.virex.wallpapers.ui.screens.pro

import android.app.Activity
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virex.wallpapers.billing.ProStatus
import com.virex.wallpapers.billing.RuStoreBillingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * PRO ViewModel
 *
 * Handles PRO upgrade via RuStore Billing. No Google Play dependencies.
 */
@HiltViewModel
class ProViewModel @Inject constructor(private val application: Application) : ViewModel() {

    // PRO status from ProStatus (single source of truth)
    val isPro: StateFlow<Boolean> =
            ProStatus.isProFlow.stateIn(
                    viewModelScope,
                    SharingStarted.Lazily,
                    ProStatus.isPro(application)
            )

    private val _price = MutableStateFlow("99 ₽")
    val price: StateFlow<String> = _price.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableSharedFlow<String>()
    val message = _message.asSharedFlow()

    init {
        loadProductPrice()
    }

    /** Load product price from RuStore */
    private fun loadProductPrice() {
        RuStoreBillingManager.getProProductInfo { product ->
            product?.let {
                // Format price from product info
                // RuStore SDK 2.x: price field - convert to String safely
                val priceStr = it.price?.toString() ?: ""
                _price.value = if (priceStr.isBlank()) "99 ₽" else priceStr
            }
        }
    }

    /** Launch PRO purchase flow via RuStore */
    fun purchase(activity: Activity) {
        if (_isLoading.value) return

        _isLoading.value = true

        RuStoreBillingManager.launchProPurchase(activity) { success, error ->
            _isLoading.value = false

            viewModelScope.launch {
                if (success) {
                    _message.emit("Добро пожаловать в VIREX PRO!")
                } else {
                    _message.emit(error ?: "Ошибка покупки. Попробуйте снова.")
                }
            }
        }
    }

    /** Restore purchases */
    fun restorePurchases() {
        _isLoading.value = true

        RuStoreBillingManager.restorePurchases { isPro ->
            _isLoading.value = false

            viewModelScope.launch {
                if (isPro) {
                    _message.emit("PRO статус восстановлен!")
                } else {
                    _message.emit("Покупки не найдены")
                }
            }
        }
    }
}
