package com.virex.wallpapers.billing

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import ru.rustore.sdk.billingclient.RuStoreBillingClient
import ru.rustore.sdk.billingclient.RuStoreBillingClientFactory
import ru.rustore.sdk.billingclient.model.product.Product
import ru.rustore.sdk.billingclient.model.purchase.PaymentResult
import ru.rustore.sdk.billingclient.model.purchase.Purchase
import ru.rustore.sdk.billingclient.model.purchase.PurchaseState
import java.lang.ref.WeakReference

/**
 * RuStore Billing Manager
 * 
 * Handles PRO version purchase through RuStore Billing SDK.
 * No Google dependencies required.
 * 
 * Features:
 * - One-time PRO purchase
 * - Automatic purchase restoration
 * - Lifecycle-safe implementation
 * - Error handling with graceful fallback
 */
object RuStoreBillingManager {
    
    private const val TAG = "RU_BILLING"
    
    // Product ID for PRO version (configure in RuStore Console)
    const val PRODUCT_ID_PRO = "pro_version"
    
    // Console ID from RuStore Developer Console
    private const val CONSOLE_APPLICATION_ID = "206365410"
    
    // RuStore Billing Client
    private var billingClient: RuStoreBillingClient? = null
    
    // Application context reference
    private var appContextRef: WeakReference<Context>? = null
    
    // Callback for purchase result
    private var purchaseCallback: ((Boolean, String?) -> Unit)? = null
    
    // Is client initialized
    private var isInitialized = false
    
    /**
     * Initialize Billing Manager
     * Call this in Application.onCreate()
     * 
     * @param application Application instance
     * @param consoleApplicationId Your app ID from RuStore Console
     */
    fun initialize(application: Application, consoleApplicationId: String = CONSOLE_APPLICATION_ID) {
        Log.e(TAG, "🚀 Initializing RuStore Billing...")
        
        appContextRef = WeakReference(application.applicationContext)
        
        try {
            billingClient = RuStoreBillingClientFactory.create(
                context = application,
                consoleApplicationId = consoleApplicationId,
                deeplinkScheme = "virexwallpapers" // For returning from payment
            )
            
            isInitialized = true
            Log.e(TAG, "✅ RuStore Billing initialized successfully")
            
            // Check for existing purchases on init
            checkProPurchase { isPro ->
                if (isPro) {
                    ProStatus.setPro(application, true)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize RuStore Billing", e)
            isInitialized = false
        }
    }
    
    /**
     * Check if RuStore is available on the device
     */
    fun isAvailable(): Boolean {
        return try {
            billingClient != null && isInitialized
        } catch (e: Exception) {
            Log.e(TAG, "Error checking availability", e)
            false
        }
    }
    
    /**
     * Launch PRO purchase flow
     * 
     * @param activity Current activity
     * @param callback Callback with (success, errorMessage)
     */
    fun launchProPurchase(
        activity: Activity,
        callback: ((success: Boolean, error: String?) -> Unit)? = null
    ) {
        if (!isInitialized || billingClient == null) {
            Log.e(TAG, "Billing not initialized")
            callback?.invoke(false, "Billing not initialized")
            return
        }
        
        if (activity.isFinishing || activity.isDestroyed) {
            Log.e(TAG, "Activity is finishing")
            callback?.invoke(false, "Activity is finishing")
            return
        }
        
        purchaseCallback = callback
        
        Log.e(TAG, "💳 Launching purchase for product: $PRODUCT_ID_PRO")
        
        billingClient?.purchases?.purchaseProduct(
            productId = PRODUCT_ID_PRO,
            orderId = null, // Auto-generated
            quantity = 1,
            developerPayload = null
        )?.addOnSuccessListener { result ->
            handlePaymentResult(result, activity)
        }?.addOnFailureListener { error ->
            Log.e(TAG, "Purchase failed: ${error.message}")
            purchaseCallback?.invoke(false, error.message)
            purchaseCallback = null
        }
    }
    
    /**
     * Handle payment result from RuStore
     */
    private fun handlePaymentResult(result: PaymentResult, activity: Activity) {
        when (result) {
            is PaymentResult.Success -> {
                Log.e(TAG, "✅ purchase success: orderId=${result.orderId}")
                
                // Confirm purchase and grant PRO
                confirmPurchaseAndGrantPro(result.orderId, activity)
            }
            
            is PaymentResult.Cancelled -> {
                Log.e(TAG, "❌ purchase cancelled by user")
                purchaseCallback?.invoke(false, "Cancelled by user")
                purchaseCallback = null
            }
            
            is PaymentResult.Failure -> {
                Log.e(TAG, "purchase failure: ${result.errorCode}")
                purchaseCallback?.invoke(false, "Error: ${result.errorCode}")
                purchaseCallback = null
            }
            
            else -> {
                Log.w(TAG, "Unknown payment result: $result")
                purchaseCallback?.invoke(false, "Unknown error")
                purchaseCallback = null
            }
        }
    }
    
    /**
     * Confirm purchase and grant PRO status
     */
    private fun confirmPurchaseAndGrantPro(orderId: String?, activity: Activity) {
        if (orderId == null) {
            Log.w(TAG, "Order ID is null, granting PRO anyway")
            ProStatus.setPro(activity.applicationContext, true)
            purchaseCallback?.invoke(true, null)
            purchaseCallback = null
            return
        }
        
        // Confirm the purchase with RuStore
        billingClient?.purchases?.confirmPurchase(
            purchaseId = orderId,
            developerPayload = null
        )?.addOnSuccessListener {
            Log.e(TAG, "✅ Purchase confirmed: $orderId")
            ProStatus.setPro(activity.applicationContext, true, orderId)
            purchaseCallback?.invoke(true, null)
            purchaseCallback = null
        }?.addOnFailureListener { error ->
            Log.e(TAG, "Failed to confirm purchase: ${error.message}")
            // Still grant PRO as purchase was successful
            ProStatus.setPro(activity.applicationContext, true, orderId)
            purchaseCallback?.invoke(true, null)
            purchaseCallback = null
        }
    }
    
    /**
     * Check if user has purchased PRO
     * Call this on app start to restore purchases
     * 
     * @param callback Callback with isPro result
     */
    fun checkProPurchase(callback: (Boolean) -> Unit) {
        if (!isInitialized || billingClient == null) {
            Log.w(TAG, "Billing not initialized, checking local status")
            val context = appContextRef?.get()
            callback(context?.let { ProStatus.isPro(it) } ?: false)
            return
        }
        
        Log.e(TAG, "🔍 Checking existing purchases...")
        
        billingClient?.purchases?.getPurchases()
            ?.addOnSuccessListener { purchases ->
                val proPurchase = findValidProPurchase(purchases)
                
                if (proPurchase != null) {
                    Log.e(TAG, "👑 restored: PRO purchase found")
                    appContextRef?.get()?.let { context ->
                        ProStatus.setPro(context, true, proPurchase.purchaseId)
                    }
                    callback(true)
                } else {
                    Log.e(TAG, "ℹ️ not purchased: No PRO purchase found")
                    callback(false)
                }
            }
            ?.addOnFailureListener { error ->
                Log.e(TAG, "error: Failed to get purchases: ${error.message}")
                // Fallback to local status
                val context = appContextRef?.get()
                callback(context?.let { ProStatus.isPro(it) } ?: false)
            }
    }
    
    /**
     * Find a valid PRO purchase from list
     */
    private fun findValidProPurchase(purchases: List<Purchase>): Purchase? {
        return purchases.find { purchase ->
            purchase.productId == PRODUCT_ID_PRO &&
            (purchase.purchaseState == PurchaseState.PAID ||
             purchase.purchaseState == PurchaseState.CONFIRMED)
        }
    }
    
    /**
     * Get PRO product info (price, description)
     * 
     * @param callback Callback with product info or null
     */
    fun getProProductInfo(callback: (Product?) -> Unit) {
        if (!isInitialized || billingClient == null) {
            callback(null)
            return
        }
        
        billingClient?.products?.getProducts(listOf(PRODUCT_ID_PRO))
            ?.addOnSuccessListener { products ->
                callback(products.firstOrNull())
            }
            ?.addOnFailureListener { error ->
                Log.e(TAG, "Failed to get product info: ${error.message}")
                callback(null)
            }
    }
    
    /**
     * Restore purchases (alias for checkProPurchase)
     */
    fun restorePurchases(callback: (Boolean) -> Unit) {
        Log.e(TAG, "🔄 Restoring purchases...")
        checkProPurchase(callback)
    }
    
    /**
     * Clean up resources
     */
    fun destroy() {
        Log.e(TAG, "🗑️ Destroying billing manager")
        purchaseCallback = null
        // billingClient doesn't require explicit cleanup
    }
}
