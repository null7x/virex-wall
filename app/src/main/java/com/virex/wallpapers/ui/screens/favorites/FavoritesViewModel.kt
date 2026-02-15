package com.virex.wallpapers.ui.screens.favorites

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virex.wallpapers.data.local.PreferencesDataStore
import com.virex.wallpapers.data.local.SyncedWallpaperDao
import com.virex.wallpapers.data.local.WallpaperDao
import com.virex.wallpapers.data.model.CachedWallpaper
import com.virex.wallpapers.data.model.Wallpaper
import com.virex.wallpapers.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Favorites ViewModel
 * 
 * Manages favorite wallpapers with:
 * - Persistent storage in Room database
 * - Support for both Firebase and synced wallpapers
 * - Offline caching for PRO users
 * - Real-time updates via Flow
 */
@HiltViewModel
class FavoritesViewModel
@Inject
constructor(
        private val repository: WallpaperRepository,
        private val wallpaperDao: WallpaperDao,
        private val syncedWallpaperDao: SyncedWallpaperDao,
        private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {
    
    companion object {
        private const val TAG = "FavoritesViewModel"
    }

    private val _favorites = MutableStateFlow<List<Wallpaper>>(emptyList())
    val favorites: StateFlow<List<Wallpaper>> = _favorites.asStateFlow()

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _cachedIds = MutableStateFlow<Set<String>>(emptySet())
    val cachedIds: StateFlow<Set<String>> = _cachedIds.asStateFlow()

    val isPro: StateFlow<Boolean> =
            preferencesDataStore.isPro.stateIn(viewModelScope, SharingStarted.Lazily, false)
    
    // Favorites count for badge
    val favoritesCount: StateFlow<Int> = 
            repository.getFavoritesCount()
                    .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    init {
        observeFavorites()
        observeCachedWallpapers()
    }

    /**
     * Observe favorites from both Firebase cache and synced wallpapers
     */
    private fun observeFavorites() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Combine favorites from regular wallpapers and synced wallpapers
            combine(
                wallpaperDao.getAllFavorites(),
                wallpaperDao.getFavoriteWallpapers(),
                syncedWallpaperDao.getAllSyncedWallpapers()
            ) { favoriteEntities, cachedFavorites, syncedWallpapers ->
                val favoriteIdSet = favoriteEntities.map { it.wallpaperId }.toSet()
                
                // Get synced wallpapers that are favorites
                val syncedFavorites = syncedWallpapers
                        .filter { favoriteIdSet.contains(it.id) }
                        .map { it.toWallpaper() }
                
                // Combine and deduplicate
                val allFavorites = (cachedFavorites + syncedFavorites)
                        .distinctBy { it.id }
                        .sortedByDescending { wallpaper ->
                            // Sort by when it was added to favorites
                            favoriteEntities.find { it.wallpaperId == wallpaper.id }?.addedAt ?: 0
                        }
                
                Pair(allFavorites, favoriteIdSet)
            }.collect { (wallpapers, ids) ->
                _favorites.value = wallpapers
                _favoriteIds.value = ids
                _isLoading.value = false
                Log.d(TAG, "Favorites updated: ${wallpapers.size} wallpapers")
            }
        }
    }
    
    /**
     * Observe cached wallpapers (for PRO offline access)
     */
    private fun observeCachedWallpapers() {
        viewModelScope.launch {
            wallpaperDao.getAllCachedWallpapers().collect { cached ->
                _cachedIds.value = cached.map { it.wallpaperId }.toSet()
            }
        }
    }

    /**
     * Toggle favorite status with animation feedback
     */
    fun toggleFavorite(wallpaperId: String) {
        viewModelScope.launch { 
            val isNowFavorite = repository.toggleFavorite(wallpaperId)
            Log.d(TAG, "Toggled favorite $wallpaperId: $isNowFavorite")
        }
    }
    
    /**
     * Remove wallpaper from favorites
     */
    fun removeFromFavorites(wallpaperId: String) {
        viewModelScope.launch {
            wallpaperDao.removeFromFavorites(wallpaperId)
            Log.d(TAG, "Removed from favorites: $wallpaperId")
        }
    }
    
    /**
     * Cache wallpaper for offline access (PRO feature)
     */
    fun cacheWallpaper(wallpaperId: String, localPath: String, fileSize: Long) {
        viewModelScope.launch {
            val cached = CachedWallpaper(
                wallpaperId = wallpaperId,
                localPath = localPath,
                fileSize = fileSize
            )
            wallpaperDao.cacheWallpaper(cached)
            Log.d(TAG, "Cached wallpaper for offline: $wallpaperId")
        }
    }
    
    /**
     * Check if a wallpaper is cached for offline access
     */
    fun isCached(wallpaperId: String): Boolean {
        return _cachedIds.value.contains(wallpaperId)
    }
    
    /**
     * Clear all favorites
     */
    fun clearAllFavorites() {
        viewModelScope.launch {
            _favoriteIds.value.forEach { id ->
                wallpaperDao.removeFromFavorites(id)
            }
            Log.d(TAG, "Cleared all favorites")
        }
    }
}
