package com.virex.wallpapers.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virex.wallpapers.data.local.PreferencesDataStore
import com.virex.wallpapers.data.model.Category
import com.virex.wallpapers.data.model.InteractionType
import com.virex.wallpapers.data.model.RecommendedWallpaper
import com.virex.wallpapers.data.model.UiState
import com.virex.wallpapers.data.model.Wallpaper
import com.virex.wallpapers.data.repository.RecommendationRepository
import com.virex.wallpapers.data.repository.SyncResult
import com.virex.wallpapers.data.repository.WallpaperRepository
import com.virex.wallpapers.data.repository.WallpaperSyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Home Screen ViewModel
 *
 * Manages state for featured, trending, new, and recommended wallpapers.
 */
@HiltViewModel
class HomeViewModel
@Inject
constructor(
        private val repository: WallpaperRepository,
        private val recommendationRepository: RecommendationRepository,
        private val preferencesDataStore: PreferencesDataStore,
        private val wallpaperSyncRepository: WallpaperSyncRepository
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _featuredWallpapers = MutableStateFlow<UiState<List<Wallpaper>>>(UiState.Loading)
    val featuredWallpapers: StateFlow<UiState<List<Wallpaper>>> = _featuredWallpapers.asStateFlow()

    private val _trendingWallpapers = MutableStateFlow<UiState<List<Wallpaper>>>(UiState.Loading)
    val trendingWallpapers: StateFlow<UiState<List<Wallpaper>>> = _trendingWallpapers.asStateFlow()

    private val _newWallpapers = MutableStateFlow<UiState<List<Wallpaper>>>(UiState.Loading)
    val newWallpapers: StateFlow<UiState<List<Wallpaper>>> = _newWallpapers.asStateFlow()

    private val _allWallpapers = MutableStateFlow<UiState<List<Wallpaper>>>(UiState.Loading)
    val allWallpapers: StateFlow<UiState<List<Wallpaper>>> = _allWallpapers.asStateFlow()

    private val _categories = MutableStateFlow<UiState<List<Category>>>(UiState.Loading)
    val categories: StateFlow<UiState<List<Category>>> = _categories.asStateFlow()

    // Recommendation states
    private val _recommendedForYou =
            MutableStateFlow<UiState<List<RecommendedWallpaper>>>(UiState.Loading)
    val recommendedForYou: StateFlow<UiState<List<RecommendedWallpaper>>> =
            _recommendedForYou.asStateFlow()

    private val _popularThisWeek =
            MutableStateFlow<UiState<List<RecommendedWallpaper>>>(UiState.Loading)
    val popularThisWeek: StateFlow<UiState<List<RecommendedWallpaper>>> =
            _popularThisWeek.asStateFlow()

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    val isPro: StateFlow<Boolean> =
            preferencesDataStore.isPro.stateIn(viewModelScope, SharingStarted.Lazily, false)

    // Jobs for cancellable loading
    private var featuredJob: Job? = null
    private var trendingJob: Job? = null
    private var newJob: Job? = null
    private var categoriesJob: Job? = null
    private var allWallpapersJob: Job? = null
    private var recommendedJob: Job? = null
    private var popularJob: Job? = null

    init {
        loadData()
        observeFavorites()
        autoLoadContent()
    }

    fun loadData() {
        loadAllWallpapers()
        loadFeaturedWallpapers()
        loadTrendingWallpapers()
        loadNewWallpapers()
        loadCategories()
        loadRecommendations()
    }

    private fun loadAllWallpapers() {
        allWallpapersJob?.cancel()
        allWallpapersJob =
                viewModelScope.launch {
                    repository.getAllWallpapers().collect { state -> _allWallpapers.value = state }
                }
    }

    private fun loadFeaturedWallpapers() {
        featuredJob?.cancel()
        featuredJob =
                viewModelScope.launch {
                    repository.getFeaturedWallpapers().collect { state ->
                        _featuredWallpapers.value = state
                    }
                }
    }

    private fun loadTrendingWallpapers() {
        trendingJob?.cancel()
        trendingJob =
                viewModelScope.launch {
                    repository.getTrendingWallpapers().collect { state ->
                        _trendingWallpapers.value = state
                    }
                }
    }

    private fun loadNewWallpapers() {
        newJob?.cancel()
        newJob =
                viewModelScope.launch {
                    repository.getNewWallpapers().collect { state -> _newWallpapers.value = state }
                }
    }

    private fun loadCategories() {
        categoriesJob?.cancel()
        categoriesJob =
                viewModelScope.launch {
                    repository.getAllCategories().collect { state -> _categories.value = state }
                }
    }

    private fun loadRecommendations() {
        // Load "Recommended for you"
        recommendedJob?.cancel()
        recommendedJob =
                viewModelScope.launch {
                    try {
                        recommendationRepository.getRecommendedForYou().collect { recommendations ->
                            _recommendedForYou.value =
                                    if (recommendations.isNotEmpty()) {
                                        Log.d(
                                                TAG,
                                                "Loaded ${recommendations.size} personalized recommendations"
                                        )
                                        UiState.Success(recommendations)
                                    } else {
                                        UiState.Empty
                                    }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to load recommendations", e)
                        _recommendedForYou.value =
                                UiState.Error(e.message ?: "Failed to load recommendations")
                    }
                }

        // Load "Popular this week"
        popularJob?.cancel()
        popularJob =
                viewModelScope.launch {
                    try {
                        recommendationRepository.getPopularThisWeek().collect { popular ->
                            _popularThisWeek.value =
                                    if (popular.isNotEmpty()) {
                                        Log.d(TAG, "Loaded ${popular.size} popular this week")
                                        UiState.Success(popular)
                                    } else {
                                        UiState.Empty
                                    }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to load popular this week", e)
                        _popularThisWeek.value =
                                UiState.Error(e.message ?: "Failed to load popular")
                    }
                }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            repository.getFavoriteWallpapers().collect { wallpapers ->
                _favorites.value = wallpapers.map { it.id }.toSet()
            }
        }
    }

    fun toggleFavorite(wallpaperId: String) {
        viewModelScope.launch { repository.toggleFavorite(wallpaperId) }
    }

    /** Track user interaction for recommendations */
    fun trackView(wallpaper: Wallpaper) {
        viewModelScope.launch {
            recommendationRepository.logInteraction(
                    wallpaperId = wallpaper.id,
                    categoryId = wallpaper.categoryId,
                    type = InteractionType.VIEW,
                    tags = wallpaper.tags
            )
        }
    }

    /**
     * Auto-load content by triggering Wallhaven sync if needed.
     */
    private fun autoLoadContent() {
        viewModelScope.launch {
            try {
                val synced = wallpaperSyncRepository.getAllSyncedWallpapers().first()
                // Sync if fewer than 50 wallpapers (starter pack only has ~12)
                if (synced.size < 50) {
                    val syncStatus = wallpaperSyncRepository.getSyncStatus().first()
                    if (syncStatus?.isSyncing == true) {
                        Log.d(TAG, "Initial sync already running, skipping duplicate trigger")
                        return@launch
                    }
                    Log.d(TAG, "Only ${synced.size} synced wallpapers, triggering sync...")
                    val result = wallpaperSyncRepository.performSync()
                    if (result is SyncResult.Success && result.newCount > 0) {
                        Log.d(TAG, "Sync fetched ${result.newCount} wallpapers, reloading UI...")
                        loadData()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Wallhaven sync failed", e)
            }
        }
    }

    fun refresh() {
        loadData()
    }
}
