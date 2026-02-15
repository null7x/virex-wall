package com.virex.wallpapers.data.repository

import android.app.Activity
import com.virex.wallpapers.billing.ProStatus
import com.virex.wallpapers.billing.RuStoreBillingManager
import com.virex.wallpapers.data.local.PreferencesDataStore
import com.virex.wallpapers.data.model.BillingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Billing Repository - RuStore Edition
 *
 * Handles RuStore In-App Purchases. One-time PRO unlock purchase.
 * No Google Play dependencies.
 */
@Singleton
class BillingRepository
@Inject
constructor(
    private val preferencesDataStore: PreferencesDataStore
) {

    companion object {
        const val PRODUCT_ID_PRO = "pro_version"
        const val PRO_PRICE = "199 ₽"  // Default price, updated from RuStore
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _billingState = MutableStateFlow<BillingState>(BillingState.Loading)
    val billingState: StateFlow<BillingState> = _billingState.asStateFlow()

    private val _formattedPrice = MutableStateFlow(PRO_PRICE)

    init {
        // Check PRO status on init
        checkProStatus()
    }

    /**
     * Check current PRO status
     */
    private fun checkProStatus() {
        scope.launch {
            RuStoreBillingManager.checkProPurchase { isPro ->
                _billingState.value = if (isPro) {
                    BillingState.Purchased
                } else {
                    BillingState.NotPurchased
                }
            }
        }
        
        // Also fetch product info for price
        RuStoreBillingManager.getProProductInfo { product ->
            product?.let {
                // RuStore Product.price is not String, convert explicitly
                _formattedPrice.value = it.price?.toString() ?: PRO_PRICE
            }
        }
    }

    /**
     * Launch purchase flow via RuStore
     */
    fun launchPurchaseFlow(activity: Activity): Boolean {
        if (!RuStoreBillingManager.isAvailable()) {
            _billingState.value = BillingState.Error("RuStore недоступен")
            return false
        }

        _billingState.value = BillingState.Loading

        RuStoreBillingManager.launchProPurchase(activity) { success, error ->
            scope.launch {
                if (success) {
                    preferencesDataStore.setProStatus(true)
                    _billingState.value = BillingState.Purchased
                } else {
                    _billingState.value = if (error?.contains("Cancelled") == true) {
                        BillingState.NotPurchased
                    } else {
                        BillingState.Error(error ?: "Ошибка покупки")
                    }
                }
            }
        }

        return true
    }

    /**
     * Restore purchases from RuStore
     */
    fun restorePurchases() {
        scope.launch {
            _billingState.value = BillingState.Loading
            
            RuStoreBillingManager.restorePurchases { isPro ->
                scope.launch {
                    if (isPro) {
                        preferencesDataStore.setProStatus(true)
                        _billingState.value = BillingState.Purchased
                    } else {
                        _billingState.value = BillingState.NotPurchased
                    }
                }
            }
        }
    }

    /**
     * Get formatted price string
     */
    fun getFormattedPrice(): String = _formattedPrice.value

    /**
     * Check if billing is ready
     */
    fun isBillingReady(): Boolean = RuStoreBillingManager.isAvailable()
}
