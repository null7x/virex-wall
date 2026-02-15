package com.virex.wallpapers

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.virex.wallpapers.ads.VkAdsManager
import com.virex.wallpapers.billing.ProStatus
import com.virex.wallpapers.billing.RuStoreBillingManager
import com.virex.wallpapers.data.local.LocaleHelper
import com.virex.wallpapers.data.repository.PreloadManager
import com.virex.wallpapers.sync.WallpaperSyncWorker
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * VIREX Wallpapers Application
 *
 * Main application class with Hilt dependency injection and optimized image loading configuration
 * for AMOLED wallpapers.
 */
@HiltAndroidApp
class VirexApp : Application(), Configuration.Provider, ImageLoaderFactory {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface LocaleHelperEntryPoint {
        fun localeHelper(): LocaleHelper
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PreloadManagerEntryPoint {
        fun preloadManager(): PreloadManager
    }

    override val workManagerConfiguration: Configuration
        get() =
                Configuration.Builder()
                        .setWorkerFactory(workerFactory)
                        .setMinimumLoggingLevel(android.util.Log.INFO)
                        .build()

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        // Initialize saved language preference
        initializeLanguage()

        // Load starter wallpapers if database is empty (for first install)
        loadPreloadData()

        // Initialize VK Ads Interstitial (preload for faster display)
        initializeAds()

        // Schedule periodic wallpaper sync
        scheduleWallpaperSync()
    }

    /**
     * Load bundled starter wallpapers on first install Ensures content is available immediately
     * without network
     */
    private fun loadPreloadData() {
        try {
            val preloadManager =
                    EntryPoints.get(this, PreloadManagerEntryPoint::class.java).preloadManager()
            applicationScope.launch(Dispatchers.IO) { preloadManager.loadPreloadDataIfEmpty() }
            Log.d("VirexApp", "Preload initialization started")
        } catch (e: Exception) {
            Log.e("VirexApp", "Failed to initialize preload", e)
        }
    }

    /**
     * Initialize VK Ads SDK for banner and interstitial ads and RuStore Billing for PRO purchases
     */
    private fun initializeAds() {
        try {
            // Initialize PRO status first (MUST be before ads)
            ProStatus.initialize(this)
            Log.e("VK_ADS", "✅ ProStatus initialized, isPro=${ProStatus.isPro(this)}")

            // Initialize RuStore Billing
            RuStoreBillingManager.initialize(this, "206365410")
            Log.e("VK_ADS", "✅ RuStore Billing initialized")

            // Initialize VK Ads (will check PRO status internally)
            VkAdsManager.initialize(this)
        } catch (e: Exception) {
            Log.e("VK_ADS", "💥 Failed to initialize ads/billing", e)
        }
    }

    /**
     * Initialize the app language from saved preference Using EntryPoints since injection isn't
     * available until after super.onCreate()
     */
    private fun initializeLanguage() {
        try {
            val localeHelper =
                    EntryPoints.get(this, LocaleHelperEntryPoint::class.java).localeHelper()
            localeHelper.initializeLanguage()
            Log.d("VirexApp", "Language initialized")
        } catch (e: Exception) {
            Log.e("VirexApp", "Failed to initialize language", e)
        }
    }

    /**
     * Schedule background wallpaper sync from Unsplash and Pexels Runs once per day on WiFi when
     * battery is not low Also triggers an immediate sync on first launch for instant content
     */
    private fun scheduleWallpaperSync() {
        try {
            WallpaperSyncWorker.schedulePeriodicSync(
                    context = this,
                    intervalHours = 24,
                    requireWifi = true
            )
            // Trigger immediate one-time sync to ensure wallpapers are available
            // even without VPN (Firebase may be blocked in some regions)
            WallpaperSyncWorker.triggerImmediateSync(this)
            Log.d("VirexApp", "Wallpaper sync scheduled + immediate sync triggered")
        } catch (e: Exception) {
            Log.e("VirexApp", "Failed to schedule wallpaper sync", e)
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
                .memoryCache {
                    MemoryCache.Builder(this)
                            .maxSizePercent(0.30) // Use 30% of available memory
                            .build()
                }
                .diskCache {
                    DiskCache.Builder()
                            .directory(cacheDir.resolve("image_cache"))
                            .maxSizeBytes(512L * 1024 * 1024) // 512 MB disk cache
                            .build()
                }
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .respectCacheHeaders(false)
                .crossfade(true)
                .crossfade(300)
                .build()
    }
}
