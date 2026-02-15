package com.virex.wallpapers.ui.screens.preview

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virex.wallpapers.data.local.PreferencesDataStore
import com.virex.wallpapers.data.model.InteractionType
import com.virex.wallpapers.data.model.RecommendedWallpaper
import com.virex.wallpapers.data.model.Wallpaper
import com.virex.wallpapers.data.model.WallpaperTarget
import com.virex.wallpapers.data.repository.RecommendationRepository
import com.virex.wallpapers.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Preview ViewModel
 *
 * Handles wallpaper preview, set, and download operations.
 * Tracks user interactions for smart recommendations.
 */
@HiltViewModel
class PreviewViewModel
@Inject
constructor(
        @ApplicationContext private val context: Context,
        private val repository: WallpaperRepository,
        private val recommendationRepository: RecommendationRepository,
        private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {

    companion object {
        private const val TAG = "PreviewViewModel"
    }

    private val _wallpaper = MutableStateFlow<Wallpaper?>(null)
    val wallpaper: StateFlow<Wallpaper?> = _wallpaper.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableSharedFlow<String>()
    val message = _message.asSharedFlow()
    
    // Similar wallpapers
    private val _similarWallpapers = MutableStateFlow<List<RecommendedWallpaper>>(emptyList())
    val similarWallpapers: StateFlow<List<RecommendedWallpaper>> = _similarWallpapers.asStateFlow()
    
    // Track view start time for engagement tracking
    private var viewStartTime: Long = 0

    val isPro: StateFlow<Boolean> =
            preferencesDataStore.isPro.stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun loadWallpaper(wallpaperId: String) {
        viewStartTime = System.currentTimeMillis()
        
        viewModelScope.launch {
            _wallpaper.value = repository.getWallpaperById(wallpaperId)

            _wallpaper.value?.let { wp ->
                // Track detail view
                trackInteraction(InteractionType.DETAIL_VIEW)
                
                // Load similar wallpapers
                loadSimilarWallpapers(wp)
            }

            // Check if favorite
            repository.isFavorite(wallpaperId).collect { isFav -> _isFavorite.value = isFav }
        }
    }
    
    /**
     * Load similar wallpapers based on current wallpaper
     */
    private fun loadSimilarWallpapers(wallpaper: Wallpaper) {
        viewModelScope.launch {
            try {
                val similar = recommendationRepository.getSimilarWallpapers(
                    wallpaperId = wallpaper.id,
                    categoryId = wallpaper.categoryId,
                    tags = wallpaper.tags,
                    limit = 10
                )
                _similarWallpapers.value = similar
                Log.d(TAG, "Loaded ${similar.size} similar wallpapers")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load similar wallpapers", e)
            }
        }
    }
    
    /**
     * Track when user leaves the preview screen
     */
    fun onLeavePreview() {
        val viewDuration = System.currentTimeMillis() - viewStartTime
        viewModelScope.launch {
            _wallpaper.value?.let { wp ->
                recommendationRepository.logInteraction(
                    wallpaperId = wp.id,
                    categoryId = wp.categoryId,
                    type = InteractionType.VIEW,
                    durationMs = viewDuration,
                    tags = wp.tags
                )
                Log.d(TAG, "Tracked view duration: ${viewDuration}ms for ${wp.id}")
            }
        }
    }
    
    /**
     * Track user interaction
     */
    private fun trackInteraction(type: InteractionType) {
        viewModelScope.launch {
            _wallpaper.value?.let { wp ->
                recommendationRepository.logInteraction(
                    wallpaperId = wp.id,
                    categoryId = wp.categoryId,
                    type = type,
                    tags = wp.tags
                )
            }
        }
    }

    fun toggleFavorite() {
        val wallpaperId = _wallpaper.value?.id ?: return
        viewModelScope.launch {
            val isNowFavorite = repository.toggleFavorite(wallpaperId)
            _isFavorite.value = isNowFavorite
            
            // Track favorite/unfavorite
            trackInteraction(if (isNowFavorite) InteractionType.FAVORITE else InteractionType.UNFAVORITE)
            
            _message.emit(if (isNowFavorite) "Added to favorites" else "Removed from favorites")
        }
    }

    fun setWallpaper(target: WallpaperTarget) {
        val wallpaper = _wallpaper.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val bitmap = downloadBitmap(wallpaper.fullUrl)
                if (bitmap != null) {
                    val wallpaperManager = WallpaperManager.getInstance(context)

                    when (target) {
                        WallpaperTarget.HOME_SCREEN -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                wallpaperManager.setBitmap(
                                        bitmap,
                                        null,
                                        true,
                                        WallpaperManager.FLAG_SYSTEM
                                )
                            } else {
                                wallpaperManager.setBitmap(bitmap)
                            }
                        }
                        WallpaperTarget.LOCK_SCREEN -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                wallpaperManager.setBitmap(
                                        bitmap,
                                        null,
                                        true,
                                        WallpaperManager.FLAG_LOCK
                                )
                            } else {
                                wallpaperManager.setBitmap(bitmap)
                            }
                        }
                        WallpaperTarget.BOTH -> {
                            wallpaperManager.setBitmap(bitmap)
                        }
                    }

                    _message.emit("Wallpaper set successfully!")
                    
                    // Track set wallpaper action
                    trackInteraction(InteractionType.SET_WALLPAPER)
                } else {
                    _message.emit("Failed to download wallpaper")
                }
            } catch (e: Exception) {
                _message.emit("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun downloadWallpaper() {
        val wallpaper = _wallpaper.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val bitmap = downloadBitmap(wallpaper.fullUrl)
                if (bitmap != null) {
                    val fileName = "VIREX_${wallpaper.id}_${System.currentTimeMillis()}.jpg"
                    val file =
                            File(
                                    context.getExternalFilesDir(
                                            android.os.Environment.DIRECTORY_PICTURES
                                    ),
                                    fileName
                            )

                    withContext(Dispatchers.IO) {
                        FileOutputStream(file).use { out ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                        }
                    }

                    // Track download
                    repository.incrementDownloadCount(wallpaper.id)
                    trackInteraction(InteractionType.DOWNLOAD)

                    _message.emit("Wallpaper saved to ${file.absolutePath}")
                } else {
                    _message.emit("Failed to download wallpaper")
                }
            } catch (e: Exception) {
                _message.emit("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun downloadBitmap(url: String): Bitmap? =
            withContext(Dispatchers.IO) {
                try {
                    val connection = URL(url).openConnection()
                    connection.connectTimeout = 30000
                    connection.readTimeout = 30000
                    connection.connect()

                    val inputStream = connection.getInputStream()
                    BitmapFactory.decodeStream(inputStream)
                } catch (e: Exception) {
                    null
                }
            }
}
