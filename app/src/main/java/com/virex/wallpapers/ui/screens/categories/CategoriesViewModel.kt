package com.virex.wallpapers.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virex.wallpapers.data.local.PreferencesDataStore
import com.virex.wallpapers.data.model.Category
import com.virex.wallpapers.data.model.UiState
import com.virex.wallpapers.data.model.Wallpaper
import com.virex.wallpapers.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Categories ViewModel */
@HiltViewModel
class CategoriesViewModel
@Inject
constructor(
        private val repository: WallpaperRepository,
        private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {

    private val _categories = MutableStateFlow<UiState<List<Category>>>(UiState.Loading)
    val categories: StateFlow<UiState<List<Category>>> = _categories.asStateFlow()

    val isPro: StateFlow<Boolean> =
            preferencesDataStore.isPro.stateIn(viewModelScope, SharingStarted.Lazily, false)

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            repository.getAllCategories().collect { state -> _categories.value = state }
        }
    }
}

/** Category Detail ViewModel */
@HiltViewModel
class CategoryDetailViewModel
@Inject
constructor(
        private val repository: WallpaperRepository,
        private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {

    private val _wallpapers = MutableStateFlow<UiState<List<Wallpaper>>>(UiState.Loading)
    val wallpapers: StateFlow<UiState<List<Wallpaper>>> = _wallpapers.asStateFlow()

    private val _categoryName = MutableStateFlow("")
    val categoryName: StateFlow<String> = _categoryName.asStateFlow()

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    val isPro: StateFlow<Boolean> =
            preferencesDataStore.isPro.stateIn(viewModelScope, SharingStarted.Lazily, false)

    init {
        observeFavorites()
    }

    fun loadCategory(categoryId: String) {
        viewModelScope.launch {
            repository.getWallpapersByCategory(categoryId).collect { state ->
                _wallpapers.value = state

                // Extract category name from first wallpaper
                if (state is UiState.Success && state.data.isNotEmpty()) {
                    _categoryName.value = state.data.first().categoryName
                }
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
}
