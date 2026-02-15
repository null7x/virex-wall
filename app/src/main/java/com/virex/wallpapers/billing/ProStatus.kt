package com.virex.wallpapers.billing

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * PRO Status Manager
 * 
 * Handles local storage of PRO purchase status.
 * Uses SharedPreferences for persistence.
 * 
 * No Google dependencies required.
 */
object ProStatus {
    
    private const val TAG = "RU_BILLING"
    
    private const val PREFS_NAME = "pro_status_prefs"
    private const val KEY_IS_PRO = "is_pro"
    private const val KEY_PURCHASE_TIME = "purchase_time"
    private const val KEY_ORDER_ID = "order_id"
    
    // Observable state for reactive UI updates
    private val _isProFlow = MutableStateFlow(false)
    val isProFlow: StateFlow<Boolean> = _isProFlow.asStateFlow()
    
    private var prefs: SharedPreferences? = null
    
    /**
     * Initialize ProStatus with application context
     * Call this in Application.onCreate()
     */
    fun initialize(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _isProFlow.value = isPro(context)
        Log.e(TAG, "✅ ProStatus initialized. isPro=${_isProFlow.value}")
    }
    
    /**
     * Check if user has PRO status
     * 
     * @param context Application or Activity context
     * @return true if user has PRO, false otherwise
     */
    fun isPro(context: Context): Boolean {
        val prefs = getPrefs(context)
        return prefs.getBoolean(KEY_IS_PRO, false)
    }
    
    /**
     * Set PRO status
     * 
     * @param context Application or Activity context
     * @param isPro true to grant PRO, false to revoke
     * @param orderId Optional order ID from RuStore
     */
    fun setPro(context: Context, isPro: Boolean, orderId: String? = null) {
        val prefs = getPrefs(context)
        prefs.edit().apply {
            putBoolean(KEY_IS_PRO, isPro)
            if (isPro) {
                putLong(KEY_PURCHASE_TIME, System.currentTimeMillis())
                orderId?.let { putString(KEY_ORDER_ID, it) }
            } else {
                remove(KEY_PURCHASE_TIME)
                remove(KEY_ORDER_ID)
            }
            apply()
        }
        
        // Update observable state
        _isProFlow.value = isPro
        
        Log.e(TAG, if (isPro) "👑 PRO status granted" else "❌ PRO status revoked")
    }
    
    /**
     * Get purchase timestamp
     */
    fun getPurchaseTime(context: Context): Long {
        return getPrefs(context).getLong(KEY_PURCHASE_TIME, 0L)
    }
    
    /**
     * Get order ID
     */
    fun getOrderId(context: Context): String? {
        return getPrefs(context).getString(KEY_ORDER_ID, null)
    }
    
    /**
     * Clear all PRO data (for testing)
     */
    fun clearAll(context: Context) {
        getPrefs(context).edit().clear().apply()
        _isProFlow.value = false
        Log.e(TAG, "🗑️ PRO status cleared")
    }
    
    private fun getPrefs(context: Context): SharedPreferences {
        return prefs ?: context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .also { prefs = it }
    }
}
