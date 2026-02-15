package com.virex.wallpapers.ui.sync

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virex.wallpapers.data.model.SyncCategory
import com.virex.wallpapers.data.model.SyncedWallpaper
import com.virex.wallpapers.data.repository.SyncResult
import com.virex.wallpapers.data.repository.WallpaperSyncRepository
import com.virex.wallpapers.sync.WallpaperSyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "SyncViewModel"

/**
 * ViewModel for Synced Wallpapers screen
 *
 * Manages:
 * - Display of synced wallpapers by category
 * - Manual sync trigger
 * - Sync status display
 */
@HiltViewModel
class SyncViewModel
@Inject
constructor(
        @ApplicationContext private val context: Context,
        private val syncRepository: WallpaperSyncRepository
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    // Selected category
    private val _selectedCategory = MutableStateFlow<SyncCategory?>(null)
    val selectedCategory: StateFlow<SyncCategory?> = _selectedCategory.asStateFlow()

    // All synced wallpapers
    val allWallpapers =
            syncRepository
                    .getAllSyncedWallpapers()
                    .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // New wallpapers (last 7 days)
    val newWallpapers =
            syncRepository
                    .getNewWallpapers()
                    .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // AMOLED wallpapers
    val amoledWallpapers =
            syncRepository
                    .getWallpapersByCategory(SyncCategory.AMOLED)
                    .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Dark wallpapers
    val darkWallpapers =
            syncRepository
                    .getWallpapersByCategory(SyncCategory.DARK)
                    .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Minimal wallpapers
    val minimalWallpapers =
            syncRepository
                    .getWallpapersByCategory(SyncCategory.MINIMAL)
                    .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Sync status
    val syncStatus =
            syncRepository.getSyncStatus().stateIn(viewModelScope, SharingStarted.Lazily, null)

    // Unviewed count for badge
    val unviewedCount =
            syncRepository.getUnviewedCount().stateIn(viewModelScope, SharingStarted.Lazily, 0)

    // Filtered wallpapers based on selected category
    val filteredWallpapers =
            combine(allWallpapers, _selectedCategory) { wallpapers, category ->
                        if (category == null) {
                            wallpapers
                        } else {
                            wallpapers.filter { it.category == category }
                        }
                    }
                    .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        // Schedule periodic sync on first launch
        schedulePeriodicSync()
        // Auto-sync if database is empty
        autoSyncIfEmpty()
    }

    /** Auto-sync if no wallpapers exist in database */
    private fun autoSyncIfEmpty() {
        viewModelScope.launch {
            try {
                val currentWallpapers = allWallpapers.first()
                if (currentWallpapers.isEmpty()) {
                    val status = syncRepository.getSyncStatus().first()
                    if (status?.isSyncing == true) {
                        Log.d(TAG, "Initial sync already running, skipping duplicate trigger")
                        return@launch
                    }
                    Log.d(TAG, "No synced wallpapers found, triggering initial sync...")
                    triggerSync()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Auto-sync check failed", e)
            }
        }
    }

    /** Schedule periodic background sync */
    private fun schedulePeriodicSync() {
        WallpaperSyncWorker.schedulePeriodicSync(
                context = context,
                intervalHours = 24,
                requireWifi = true
        )
    }

    /** Trigger manual sync */
    fun triggerSync() {
        Log.d(TAG, "triggerSync() called")
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, error = null, message = null)

            Log.d(TAG, "Starting sync...")
            val result = syncRepository.performSync()
            Log.d(TAG, "Sync result: $result")

            when (result) {
                is SyncResult.Success -> {
                    Log.d(TAG, "Sync success: ${result.newCount} new wallpapers")
                    _uiState.value =
                            _uiState.value.copy(
                                    isSyncing = false,
                                    message =
                                            if (result.newCount > 0) {
                                                "Synced ${result.newCount} new wallpapers!"
                                            } else {
                                                "Already up to date"
                                            }
                            )
                }
                is SyncResult.Error -> {
                    Log.e(TAG, "Sync error: ${result.message}")
                    _uiState.value = _uiState.value.copy(isSyncing = false, error = result.message)
                }
            }
        }
    }

    /** Trigger immediate background sync */
    fun triggerBackgroundSync() {
        WallpaperSyncWorker.triggerImmediateSync(context)
        _uiState.value = _uiState.value.copy(message = "Sync scheduled in background")
    }

    /** Select category filter */
    fun selectCategory(category: SyncCategory?) {
        _selectedCategory.value = category
    }

    /** Mark wallpaper as viewed */
    fun markAsViewed(wallpaperId: String) {
        viewModelScope.launch { syncRepository.markAsViewed(wallpaperId) }
    }

    /** Get wallpaper by ID */
    suspend fun getWallpaper(id: String): SyncedWallpaper? {
        return syncRepository.getWallpaperById(id)
    }

    /** Track download (for Unsplash attribution) */
    fun trackDownload(wallpaper: SyncedWallpaper) {
        if (wallpaper.source == com.virex.wallpapers.data.model.WallpaperSource.UNSPLASH) {
            viewModelScope.launch { syncRepository.trackUnsplashDownload(wallpaper.sourceId) }
        }
    }

    /** Clear message */
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    /** Clear error */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /** Clear all synced wallpapers (for testing) */
    fun clearAllWallpapers() {
        viewModelScope.launch { syncRepository.clearAllSyncedWallpapers() }
    }
}

/** UI State for Sync screen */
data class SyncUiState(
        val isSyncing: Boolean = false,
        val error: String? = null,
        val message: String? = null
)
