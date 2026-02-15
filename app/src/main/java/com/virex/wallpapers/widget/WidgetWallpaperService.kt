package com.virex.wallpapers.widget

import android.app.Service
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.virex.wallpapers.data.local.SyncedWallpaperDao
import com.virex.wallpapers.data.local.WallpaperDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import javax.inject.Inject

/**
 * Service for setting wallpapers from widget
 * 
 * Handles:
 * - Setting random AMOLED wallpaper
 * - Setting random favorite wallpaper
 * 
 * Runs quickly in background without blocking UI.
 */
@AndroidEntryPoint
class WidgetWallpaperService : Service() {

    companion object {
        private const val TAG = "WidgetWallpaperService"
        const val ACTION_SET_RANDOM = "com.virex.wallpapers.widget.SET_RANDOM"
        const val ACTION_SET_FAVORITE = "com.virex.wallpapers.widget.SET_FAVORITE"
    }

    @Inject
    lateinit var syncedWallpaperDao: SyncedWallpaperDao
    
    @Inject
    lateinit var wallpaperDao: WallpaperDao

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SET_RANDOM -> setRandomWallpaper()
            ACTION_SET_FAVORITE -> setFavoriteWallpaper()
        }
        return START_NOT_STICKY
    }

    private fun setRandomWallpaper() {
        serviceScope.launch {
            try {
                showToast("Setting random wallpaper...")
                
                // Get random synced wallpaper
                val syncedWallpapers = syncedWallpaperDao.getAllSyncedWallpapers().first()
                
                if (syncedWallpapers.isEmpty()) {
                    showToast("No wallpapers available. Open the app to sync.")
                    stopSelf()
                    return@launch
                }
                
                val randomWallpaper = syncedWallpapers.random()
                val success = setWallpaperFromUrl(randomWallpaper.fullUrl)
                
                if (success) {
                    val name = randomWallpaper.photographerName.ifBlank { "AMOLED" }
                    showToast("Wallpaper by $name set!")
                } else {
                    showToast("Failed to set wallpaper")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting random wallpaper", e)
                showToast("Error: ${e.message}")
            } finally {
                stopSelf()
            }
        }
    }

    private fun setFavoriteWallpaper() {
        serviceScope.launch {
            try {
                showToast("Setting favorite wallpaper...")
                
                // Get favorite wallpapers
                val favorites = wallpaperDao.getFavoriteWallpapers().first()
                
                if (favorites.isEmpty()) {
                    // Try synced wallpapers that are favorited
                    val favoriteIds = wallpaperDao.getAllFavorites().first().map { it.wallpaperId }
                    val syncedWallpapers = syncedWallpaperDao.getAllSyncedWallpapers().first()
                    val syncedFavorites = syncedWallpapers.filter { favoriteIds.contains(it.id) }
                    
                    if (syncedFavorites.isEmpty()) {
                        showToast("No favorites yet. Add some first!")
                        stopSelf()
                        return@launch
                    }
                    
                    val randomFavorite = syncedFavorites.random()
                    val success = setWallpaperFromUrl(randomFavorite.fullUrl)
                    
                    if (success) {
                        showToast("Favorite wallpaper set!")
                    } else {
                        showToast("Failed to set wallpaper")
                    }
                } else {
                    val randomFavorite = favorites.random()
                    val success = setWallpaperFromUrl(randomFavorite.fullUrl)
                    
                    if (success) {
                        showToast("Favorite wallpaper set!")
                    } else {
                        showToast("Failed to set wallpaper")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting favorite wallpaper", e)
                showToast("Error: ${e.message}")
            } finally {
                stopSelf()
            }
        }
    }

    private suspend fun setWallpaperFromUrl(url: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection()
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.connect()

                val inputStream = connection.getInputStream()
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                if (bitmap != null) {
                    val wallpaperManager = WallpaperManager.getInstance(applicationContext)
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        // Set on both home and lock screen
                        wallpaperManager.setBitmap(
                            bitmap,
                            null,
                            true,
                            WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                        )
                    } else {
                        wallpaperManager.setBitmap(bitmap)
                    }
                    
                    bitmap.recycle()
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading/setting wallpaper", e)
                false
            }
        }
    }

    private fun showToast(message: String) {
        serviceScope.launch(Dispatchers.Main) {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
