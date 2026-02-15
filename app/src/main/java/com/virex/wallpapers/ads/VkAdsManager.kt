package com.virex.wallpapers.ads

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import com.my.target.ads.InterstitialAd
import com.my.target.ads.MyTargetView
import com.my.target.common.models.IAdLoadingError
import com.virex.wallpapers.billing.ProStatus
import java.lang.ref.WeakReference

/**
 * VK Ads (myTarget) Production Manager
 * 
 * SLOT IDs from VK Ads cabinet:
 * - Banner: 1974334
 * - Interstitial: 1974337
 * 
 * Features:
 * - Singleton pattern
 * - Automatic preload
 * - Anti-spam timing (120s minimum interval)
 * - PRO user check
 * - Smart reload on no-fill
 * - Full logging via Log.e (visible in release)
 */
object VkAdsManager {
    
    private const val TAG = "VK_ADS"
    
    // ═══════════════════════════════════════════════════════════════════════════
    // PRODUCTION SLOT IDs - FROM VK ADS CABINET
    // ═══════════════════════════════════════════════════════════════════════════
    const val BANNER_SLOT_ID = 1974334
    const val INTERSTITIAL_SLOT_ID = 1974337
    
    // Anti-spam: minimum interval between interstitials (milliseconds)
    private const val MIN_INTERSTITIAL_INTERVAL_MS = 120_000L  // 2 minutes
    
    // Exponential backoff for no-fill retry: 30s → 60s → 120s → max 120s
    private const val BACKOFF_BASE_MS = 30_000L    // 30 seconds base
    private const val BACKOFF_MAX_MS = 120_000L    // 2 minutes max
    private const val MAX_RETRY_COUNT = 3          // Max retries before giving up
    
    // Preferences
    private const val PREFS_NAME = "vk_ads_prefs"
    private const val KEY_LAST_INTERSTITIAL_TIME = "last_interstitial_time"
    
    // Backoff state
    private var bannerRetryCount = 0
    private var interstitialRetryCount = 0
    
    // State
    private var isInitialized = false
    private var appContextRef: WeakReference<Context>? = null
    private var prefs: SharedPreferences? = null
    
    // Interstitial state
    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoaded = false
    private var isInterstitialShowing = false
    
    // Banner state (weak reference to prevent leaks)
    private var currentBannerRef: WeakReference<MyTargetView>? = null
    
    private val mainHandler = Handler(Looper.getMainLooper())
    
    // ═══════════════════════════════════════════════════════════════════════════
    // INITIALIZATION
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Initialize VK Ads SDK
     * Call in Application.onCreate() AFTER ProStatus.initialize()
     */
    fun initialize(context: Context) {
        if (isInitialized) {
            Log.e(TAG, "⚠️ INIT: Already initialized, skipping")
            return
        }
        
        // PRO users don't need ads
        if (ProStatus.isPro(context)) {
            Log.e(TAG, "👑 INIT: PRO user detected, ads disabled")
            return
        }
        
        Log.e(TAG, "🚀 INIT: Starting VK Ads initialization")
        Log.e(TAG, "📊 INIT: Banner SLOT_ID=$BANNER_SLOT_ID, Interstitial SLOT_ID=$INTERSTITIAL_SLOT_ID")
        
        appContextRef = WeakReference(context.applicationContext)
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        isInitialized = true
        
        // Preload interstitial immediately
        preloadInterstitial()
        
        Log.e(TAG, "✅ INIT: VK Ads initialized successfully")
    }
    
    /**
     * Check if ads should be shown (not PRO, initialized)
     */
    fun shouldShowAds(context: Context): Boolean {
        if (!isInitialized) {
            Log.e(TAG, "⚠️ shouldShowAds: Not initialized")
            return false
        }
        if (ProStatus.isPro(context)) {
            Log.e(TAG, "👑 shouldShowAds: PRO user, no ads")
            return false
        }
        return true
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // BANNER ADS
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Create and load a banner ad
     * 
     * @param context Activity or Application context
     * @param container ViewGroup to attach banner (FrameLayout recommended)
     * @param onLoaded Callback when banner is ready
     * @param onError Callback on failure
     */
    fun loadBanner(
        context: Context,
        container: ViewGroup,
        onLoaded: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ): MyTargetView? {
        
        if (!shouldShowAds(context)) {
            Log.e(TAG, "🚫 BANNER: Ads disabled, not loading")
            return null
        }
        
        Log.e(TAG, "📦 BANNER: Creating banner with SLOT_ID=$BANNER_SLOT_ID")
        
        // Destroy previous banner
        currentBannerRef?.get()?.let { oldBanner ->
            Log.e(TAG, "🗑️ BANNER: Destroying previous banner")
            (oldBanner.parent as? ViewGroup)?.removeView(oldBanner)
            oldBanner.destroy()
        }
        
        val adView = MyTargetView(context).apply {
            setSlotId(BANNER_SLOT_ID)
        }
        
        currentBannerRef = WeakReference(adView)
        
        adView.setListener(object : MyTargetView.MyTargetViewListener {
            override fun onLoad(view: MyTargetView) {
                Log.e(TAG, "✅ BANNER: Loaded successfully!")
                
                // Attach to container
                mainHandler.post {
                    attachBannerToContainer(view, container)
                }
                
                onLoaded?.invoke()
                bannerRetryCount = 0  // Reset on success
            }
            
            override fun onNoAd(error: IAdLoadingError, view: MyTargetView) {
                Log.e(TAG, "❌ BANNER: No fill - ${error.message} (code: ${error.code})")
                
                // Exponential backoff: 30s → 60s → 120s, max 3 retries
                if (bannerRetryCount < MAX_RETRY_COUNT) {
                    val delay = calculateBackoffDelay(bannerRetryCount)
                    bannerRetryCount++
                    Log.e(TAG, "⏰ BANNER: Retry $bannerRetryCount/$MAX_RETRY_COUNT in ${delay/1000}s")
                    
                    mainHandler.postDelayed({
                        Log.e(TAG, "🔄 BANNER: Retrying load (attempt $bannerRetryCount)...")
                        view.load()
                    }, delay)
                } else {
                    Log.e(TAG, "🛑 BANNER: Max retries reached, stopping")
                }
                
                onError?.invoke(error.message ?: "No ad available")
            }
            
            override fun onClick(view: MyTargetView) {
                Log.e(TAG, "👆 BANNER: Clicked")
            }
            
            override fun onShow(view: MyTargetView) {
                Log.e(TAG, "👁️ BANNER: Shown - IMPRESSION COUNTED")
                bannerRetryCount = 0  // Reset on successful show
            }
        })
        
        // Add to container BEFORE loading (required for proper sizing)
        attachBannerToContainer(adView, container)
        
        Log.e(TAG, "🔄 BANNER: Starting load...")
        adView.load()
        
        return adView
    }
    
    /**
     * Attach banner to container with proper layout
     */
    private fun attachBannerToContainer(adView: MyTargetView, container: ViewGroup) {
        // Remove from previous parent
        (adView.parent as? ViewGroup)?.removeView(adView)
        
        // Clear container
        container.removeAllViews()
        
        // Add with proper params (320x50 dp is standard banner)
        val density = container.context.resources.displayMetrics.density
        val heightPx = (50 * density).toInt()
        
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            heightPx
        )
        container.addView(adView, params)
        
        Log.e(TAG, "📐 BANNER: Attached to container, height=${heightPx}px")
    }
    
    /**
     * Destroy current banner
     */
    fun destroyBanner() {
        currentBannerRef?.get()?.let { banner ->
            Log.e(TAG, "🗑️ BANNER: Destroying")
            (banner.parent as? ViewGroup)?.removeView(banner)
            banner.destroy()
        }
        currentBannerRef = null
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // INTERSTITIAL ADS
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Preload interstitial for future display
     */
    fun preloadInterstitial() {
        val context = appContextRef?.get() ?: run {
            Log.e(TAG, "⚠️ INTERSTITIAL: Cannot preload - no context")
            return
        }
        
        if (!shouldShowAds(context)) {
            return
        }
        
        if (isInterstitialLoaded) {
            Log.e(TAG, "⚠️ INTERSTITIAL: Already loaded, skipping preload")
            return
        }
        
        if (interstitialAd != null) {
            Log.e(TAG, "⚠️ INTERSTITIAL: Load in progress, skipping")
            return
        }
        
        Log.e(TAG, "📦 INTERSTITIAL: Preloading with SLOT_ID=$INTERSTITIAL_SLOT_ID")
        
        interstitialAd = InterstitialAd(INTERSTITIAL_SLOT_ID, context).apply {
            setListener(InterstitialListener())
            load()
        }
    }
    
    /**
     * Show interstitial if loaded and timing allows
     * 
     * @param activity Current activity (required for showing)
     * @return true if shown, false if skipped
     */
    fun showInterstitial(activity: Activity): Boolean {
        Log.e(TAG, "🎬 INTERSTITIAL: showInterstitial() called")
        
        // Safety checks
        if (activity.isFinishing || activity.isDestroyed) {
            Log.e(TAG, "⚠️ INTERSTITIAL: Activity is finishing, skipping")
            return false
        }
        
        if (!shouldShowAds(activity)) {
            return false
        }
        
        if (isInterstitialShowing) {
            Log.e(TAG, "⚠️ INTERSTITIAL: Already showing, skipping")
            return false
        }
        
        if (!isInterstitialLoaded || interstitialAd == null) {
            Log.e(TAG, "⚠️ INTERSTITIAL: Not loaded yet, preloading...")
            preloadInterstitial()
            return false
        }
        
        // Anti-spam timing check
        if (!hasInterstitialIntervalPassed()) {
            val remaining = getRemainingInterstitialCooldown()
            Log.e(TAG, "⏱️ INTERSTITIAL: Cooldown active, ${remaining}s remaining")
            return false
        }
        
        // Show ad
        return try {
            Log.e(TAG, "🎬 INTERSTITIAL: SHOWING NOW!")
            isInterstitialShowing = true
            interstitialAd?.show(activity)
            updateLastInterstitialTime()
            true
        } catch (e: Exception) {
            Log.e(TAG, "💥 INTERSTITIAL: Error showing - ${e.message}")
            isInterstitialShowing = false
            false
        }
    }
    
    /**
     * Check if enough time has passed since last interstitial
     */
    private fun hasInterstitialIntervalPassed(): Boolean {
        val lastShow = prefs?.getLong(KEY_LAST_INTERSTITIAL_TIME, 0L) ?: 0L
        val elapsed = System.currentTimeMillis() - lastShow
        val passed = elapsed >= MIN_INTERSTITIAL_INTERVAL_MS
        
        Log.e(TAG, "⏱️ THROTTLE: elapsed=${elapsed/1000}s, min=${MIN_INTERSTITIAL_INTERVAL_MS/1000}s, allowed=$passed")
        
        return passed
    }
    
    /**
     * Get remaining cooldown in seconds
     */
    private fun getRemainingInterstitialCooldown(): Long {
        val lastShow = prefs?.getLong(KEY_LAST_INTERSTITIAL_TIME, 0L) ?: 0L
        val nextAllowed = lastShow + MIN_INTERSTITIAL_INTERVAL_MS
        return maxOf(0, (nextAllowed - System.currentTimeMillis()) / 1000)
    }
    
    /**
     * Update last interstitial show time
     */
    private fun updateLastInterstitialTime() {
        prefs?.edit()?.putLong(KEY_LAST_INTERSTITIAL_TIME, System.currentTimeMillis())?.apply()
    }
    
    /**
     * Check if interstitial is ready to show
     */
    fun isInterstitialReady(): Boolean {
        return isInterstitialLoaded && 
               interstitialAd != null && 
               !isInterstitialShowing && 
               hasInterstitialIntervalPassed()
    }
    
    /**
     * Get debug info string
     */
    fun getDebugInfo(): String {
        val context = appContextRef?.get()
        return buildString {
            append("VK_ADS_DEBUG: ")
            append("init=$isInitialized, ")
            append("pro=${context?.let { ProStatus.isPro(it) } ?: "?"}, ")
            append("interstitialLoaded=$isInterstitialLoaded, ")
            append("showing=$isInterstitialShowing, ")
            append("cooldownOk=${hasInterstitialIntervalPassed()}")
        }
    }
    
    /**
     * Calculate exponential backoff delay: 30s → 60s → 120s
     * @param retryCount Current retry attempt (0-based)
     * @return Delay in milliseconds
     */
    private fun calculateBackoffDelay(retryCount: Int): Long {
        val delay = BACKOFF_BASE_MS * (1 shl retryCount.coerceAtMost(2))
        return delay.coerceAtMost(BACKOFF_MAX_MS)
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // INTERSTITIAL LISTENER
    // ═══════════════════════════════════════════════════════════════════════════
    
    private class InterstitialListener : InterstitialAd.InterstitialAdListener {
        
        override fun onLoad(ad: InterstitialAd) {
            Log.e(TAG, "✅ INTERSTITIAL: Loaded and ready!")
            isInterstitialLoaded = true
            interstitialRetryCount = 0  // Reset on success
        }
        
        override fun onNoAd(error: IAdLoadingError, ad: InterstitialAd) {
            Log.e(TAG, "❌ INTERSTITIAL: No fill - ${error.message} (code: ${error.code})")
            isInterstitialLoaded = false
            interstitialAd = null
            
            // Exponential backoff: 30s → 60s → 120s, max 3 retries
            if (interstitialRetryCount < MAX_RETRY_COUNT) {
                val delay = calculateBackoffDelay(interstitialRetryCount)
                interstitialRetryCount++
                Log.e(TAG, "⏰ INTERSTITIAL: Retry $interstitialRetryCount/$MAX_RETRY_COUNT in ${delay/1000}s")
                
                mainHandler.postDelayed({
                    Log.e(TAG, "🔄 INTERSTITIAL: Retrying load (attempt $interstitialRetryCount)...")
                    preloadInterstitial()
                }, delay)
            } else {
                Log.e(TAG, "🛑 INTERSTITIAL: Max retries reached, stopping")
            }
        }
        
        override fun onClick(ad: InterstitialAd) {
            Log.e(TAG, "👆 INTERSTITIAL: Clicked")
        }
        
        override fun onDisplay(ad: InterstitialAd) {
            Log.e(TAG, "👁️ INTERSTITIAL: Displayed - IMPRESSION COUNTED")
            isInterstitialShowing = true
        }
        
        override fun onDismiss(ad: InterstitialAd) {
            Log.e(TAG, "✖️ INTERSTITIAL: Dismissed by user")
            isInterstitialShowing = false
            isInterstitialLoaded = false
            interstitialAd = null
            interstitialRetryCount = 0  // Reset on successful show/dismiss
            
            // Preload next ad immediately
            mainHandler.post {
                Log.e(TAG, "🔄 INTERSTITIAL: Preloading next...")
                preloadInterstitial()
            }
        }
        
        override fun onVideoCompleted(ad: InterstitialAd) {
            Log.e(TAG, "🎬 INTERSTITIAL: Video completed")
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // CLEANUP
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Clean up all ad resources
     */
    fun destroy() {
        Log.e(TAG, "🗑️ DESTROY: Cleaning up all ads")
        
        // Destroy banner
        destroyBanner()
        
        // Destroy interstitial
        interstitialAd?.dismiss()
        interstitialAd = null
        isInterstitialLoaded = false
        isInterstitialShowing = false
        
        isInitialized = false
    }
}
