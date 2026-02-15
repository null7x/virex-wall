package com.virex.wallpapers.data.repository

import com.virex.wallpapers.data.local.PreferencesDataStore
import com.virex.wallpapers.data.local.SyncedWallpaperDao
import com.virex.wallpapers.data.local.WallpaperDao
import com.virex.wallpapers.data.model.Category
import com.virex.wallpapers.data.model.FavoriteWallpaper
import com.virex.wallpapers.data.model.SyncCategory
import com.virex.wallpapers.data.model.UiState
import com.virex.wallpapers.data.model.Wallpaper
import com.virex.wallpapers.data.remote.FirebaseDataSource
import com.virex.wallpapers.data.remote.api.VirexBackendApi
import com.virex.wallpapers.data.remote.model.VirexWallpaper
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Wallpaper Repository
 *
 * Single source of truth for wallpaper data. 
 * PRIMARY: VIREX Backend (works in Russia without VPN)
 * FALLBACK: Local synced data
 */
@Singleton
class WallpaperRepository
@Inject
constructor(
        private val firebaseDataSource: FirebaseDataSource,
        private val wallpaperDao: WallpaperDao,
        private val syncedWallpaperDao: SyncedWallpaperDao,
        private val preferencesDataStore: PreferencesDataStore,
        @Named("virex_backend") private val virexBackendApi: VirexBackendApi
) {
    
    companion object {
        private const val TAG = "WallpaperRepo"
    }
    
    /** Convert VirexWallpaper to Wallpaper with category from tags */
    private fun VirexWallpaper.toWallpaper(): Wallpaper {
        val (catId, catName) = determineCategoryFromTags(tags)
        return Wallpaper(
            id = id,
            title = title,
            thumbnailUrl = thumbnailUrl,
            fullUrl = url,
            categoryId = catId,
            categoryName = catName,
            tags = tags ?: emptyList(),
            width = width,
            height = height,
            likes = 0,
            downloads = 0,
            source = source
        )
    }
    
    /** Determine category from wallpaper tags */
    private fun determineCategoryFromTags(tags: List<String>?): Pair<String, String> {
        if (tags.isNullOrEmpty()) return "general" to "General"
        val tagsLower = tags.map { it.lowercase() }
        return when {
            tagsLower.any { it.contains("nature") || it.contains("landscape") || it.contains("forest") } -> "nature" to "Nature"
            tagsLower.any { it.contains("abstract") || it.contains("pattern") || it.contains("geometric") } -> "abstract" to "Abstract"
            tagsLower.any { it.contains("minimal") || it.contains("simple") || it.contains("clean") } -> "minimal" to "Minimal"
            tagsLower.any { it.contains("dark") || it.contains("black") || it.contains("amoled") } -> "amoled" to "AMOLED"
            tagsLower.any { it.contains("space") || it.contains("galaxy") || it.contains("stars") } -> "space" to "Space"
            tagsLower.any { it.contains("city") || it.contains("urban") || it.contains("architecture") } -> "city" to "City"
            tagsLower.any { it.contains("anime") || it.contains("manga") || it.contains("illustration") } -> "anime" to "Anime"
            tagsLower.any { it.contains("cyberpunk") || it.contains("neon") || it.contains("futuristic") } -> "cyberpunk" to "Cyberpunk"
            tagsLower.any { it.contains("car") || it.contains("vehicle") || it.contains("automotive") } -> "cars" to "Cars"
            tagsLower.any { it.contains("fantasy") || it.contains("dragon") || it.contains("magical") } -> "fantasy" to "Fantasy"
            tagsLower.any { it.contains("game") || it.contains("gaming") } -> "gaming" to "Gaming"
            tagsLower.any { it.contains("ocean") || it.contains("sea") || it.contains("water") } -> "ocean" to "Ocean"
            tagsLower.any { it.contains("mountain") || it.contains("peak") } -> "mountain" to "Mountain"
            tagsLower.any { it.contains("flower") || it.contains("floral") } -> "flowers" to "Flowers"
            else -> "general" to "General"
        }
    }

    // ==================== WALLPAPERS ====================

    /**
     * Get all wallpapers from VIREX Backend (PRIMARY)
     * Falls back to local synced data if backend unavailable
     */
    fun getAllWallpapers(): Flow<UiState<List<Wallpaper>>> = flow {
        emit(UiState.Loading)
        
        try {
            // PRIMARY: Load from VIREX Backend
            android.util.Log.d(TAG, "Loading wallpapers from VIREX Backend...")
            val response = withTimeoutOrNull(15_000L) {
                virexBackendApi.getTrending(page = 1, perPage = 100)
            }
            
            if (response != null && response.wallpapers.isNotEmpty()) {
                val wallpapers = response.wallpapers.map { it.toWallpaper() }
                android.util.Log.d(TAG, "Backend returned ${wallpapers.size} wallpapers")
                
                // Also merge in local synced wallpapers for more content
                val synced = syncedWallpaperDao.getAllSyncedWallpapersList()
                val syncedWallpapers = synced.map { it.toWallpaper() }
                
                val allIds = wallpapers.map { it.id }.toSet()
                val extraLocal = syncedWallpapers.filter { it.id !in allIds }
                
                val combined = wallpapers + extraLocal
                android.util.Log.d(TAG, "Combined: ${combined.size} wallpapers (${wallpapers.size} backend + ${extraLocal.size} local)")
                emit(UiState.Success(combined))
            } else {
                // Fallback to local
                android.util.Log.w(TAG, "Backend empty or timeout, falling back to local")
                emitLocalWallpapers()
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Backend error: ${e.message}, falling back to local", e)
            emitLocalWallpapers()
        }
    }.catch { e ->
        android.util.Log.e(TAG, "getAllWallpapers error", e)
        emit(UiState.Error("Не удалось загрузить обои. Проверьте подключение"))
    }
    
    private suspend fun kotlinx.coroutines.flow.FlowCollector<UiState<List<Wallpaper>>>.emitLocalWallpapers() {
        val synced = syncedWallpaperDao.getAllSyncedWallpapersList()
        val wallpapers = synced.map { it.toWallpaper() }
        if (wallpapers.isNotEmpty()) {
            emit(UiState.Success(wallpapers))
        } else {
            emit(UiState.Empty)
        }
    }

    /** Get wallpapers by category from VIREX Backend */
    fun getWallpapersByCategory(categoryId: String): Flow<UiState<List<Wallpaper>>> = flow {
        emit(UiState.Loading)
        android.util.Log.d(TAG, "getWallpapersByCategory($categoryId)")

        try {
            // PRIMARY: Load from VIREX Backend
            val response = virexBackendApi.getByCategory(categoryId, page = 1, perPage = 50)
            
            if (response.wallpapers.isNotEmpty()) {
                val wallpapers = response.wallpapers.map { it.toWallpaper() }
                android.util.Log.d(TAG, "Backend category $categoryId: ${wallpapers.size} wallpapers")
                emit(UiState.Success(wallpapers))
            } else {
                // Fallback to local
                emitLocalCategoryWallpapers(categoryId)
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Backend category error: ${e.message}", e)
            emitLocalCategoryWallpapers(categoryId)
        }
    }.catch { e ->
        android.util.Log.e(TAG, "getWallpapersByCategory error", e)
        emit(UiState.Error("Не удалось загрузить обои"))
    }
    
    private suspend fun kotlinx.coroutines.flow.FlowCollector<UiState<List<Wallpaper>>>.emitLocalCategoryWallpapers(categoryId: String) {
        val syncCategory = try {
            SyncCategory.valueOf(categoryId.uppercase())
        } catch (_: Exception) {
            SyncCategory.values().find {
                it.name.equals(categoryId, ignoreCase = true) ||
                        it.displayName.equals(categoryId, ignoreCase = true)
            }
        }

        if (syncCategory != null) {
            val synced = syncedWallpaperDao.getWallpapersByCategoryList(syncCategory)
            val wallpapers = synced.map { it.toWallpaper() }
            if (wallpapers.isNotEmpty()) {
                emit(UiState.Success(wallpapers))
            } else {
                emit(UiState.Empty)
            }
        } else {
            emit(UiState.Empty)
        }
    }

    /**
     * Get featured wallpapers from VIREX Backend
     */
    fun getFeaturedWallpapers(): Flow<UiState<List<Wallpaper>>> = flow {
        emit(UiState.Loading)
        
        try {
            // Use search for featured/popular
            val response = virexBackendApi.searchWallpapers(query = "featured", page = 1, perPage = 30)
            
            if (response.wallpapers.isNotEmpty()) {
                val wallpapers = response.wallpapers.map { it.toWallpaper() }
                emit(UiState.Success(wallpapers))
            } else {
                // Fallback: get trending and shuffle
                val trending = virexBackendApi.getTrending(page = 1, perPage = 50)
                if (trending.wallpapers.isNotEmpty()) {
                    val wallpapers = trending.wallpapers.shuffled().take(30).map { it.toWallpaper() }
                    emit(UiState.Success(wallpapers))
                } else {
                    emitLocalFeatured()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "getFeaturedWallpapers error: ${e.message}", e)
            emitLocalFeatured()
        }
    }.catch { e ->
        android.util.Log.e(TAG, "getFeaturedWallpapers error", e)
        emit(UiState.Error("Не удалось загрузить обои"))
    }
    
    private suspend fun kotlinx.coroutines.flow.FlowCollector<UiState<List<Wallpaper>>>.emitLocalFeatured() {
        val synced = syncedWallpaperDao.getAllSyncedWallpapersList()
        val wallpapers = synced.shuffled().take(30).map { it.toWallpaper() }
        if (wallpapers.isNotEmpty()) {
            emit(UiState.Success(wallpapers))
        } else {
            emit(UiState.Empty)
        }
    }

    /**
     * Get trending wallpapers from VIREX Backend
     */
    fun getTrendingWallpapers(): Flow<UiState<List<Wallpaper>>> = flow {
        emit(UiState.Loading)
        
        try {
            val response = virexBackendApi.getTrending(page = 1, perPage = 30)
            
            if (response.wallpapers.isNotEmpty()) {
                val wallpapers = response.wallpapers.map { it.toWallpaper() }
                android.util.Log.d(TAG, "Trending: ${wallpapers.size} wallpapers from backend")
                emit(UiState.Success(wallpapers))
            } else {
                emitLocalTrending()
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "getTrendingWallpapers error: ${e.message}", e)
            emitLocalTrending()
        }
    }.catch { e ->
        android.util.Log.e(TAG, "getTrendingWallpapers error", e)
        emit(UiState.Error("Не удалось загрузить обои"))
    }
    
    private suspend fun kotlinx.coroutines.flow.FlowCollector<UiState<List<Wallpaper>>>.emitLocalTrending() {
        val synced = syncedWallpaperDao.getAllSyncedWallpapersList()
        val wallpapers = synced.sortedByDescending { it.likes }.take(30).map { it.toWallpaper() }
        if (wallpapers.isNotEmpty()) {
            emit(UiState.Success(wallpapers))
        } else {
            emit(UiState.Empty)
        }
    }

    /**
     * Get new wallpapers from VIREX Backend
     */
    fun getNewWallpapers(): Flow<UiState<List<Wallpaper>>> = flow {
        emit(UiState.Loading)
        
        try {
            // Use search for latest/new wallpapers
            val response = virexBackendApi.searchWallpapers(query = "latest", page = 1, perPage = 30)
            
            if (response.wallpapers.isNotEmpty()) {
                val wallpapers = response.wallpapers.map { it.toWallpaper() }
                android.util.Log.d(TAG, "New: ${wallpapers.size} wallpapers from backend")
                emit(UiState.Success(wallpapers))
            } else {
                // Fallback to trending (newest)
                val trending = virexBackendApi.getTrending(page = 1, perPage = 50)
                if (trending.wallpapers.isNotEmpty()) {
                    val wallpapers = trending.wallpapers.take(30).map { it.toWallpaper() }
                    emit(UiState.Success(wallpapers))
                } else {
                    emitLocalNew()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "getNewWallpapers error: ${e.message}", e)
            emitLocalNew()
        }
    }.catch { e ->
        android.util.Log.e(TAG, "getNewWallpapers error", e)
        emit(UiState.Error("Не удалось загрузить обои"))
    }
    
    private suspend fun kotlinx.coroutines.flow.FlowCollector<UiState<List<Wallpaper>>>.emitLocalNew() {
        val synced = syncedWallpaperDao.getAllSyncedWallpapersList()
        val wallpapers = synced.sortedByDescending { it.syncedAt }.take(30).map { it.toWallpaper() }
        if (wallpapers.isNotEmpty()) {
            emit(UiState.Success(wallpapers))
        } else {
            emit(UiState.Empty)
        }
    }

    /** Get wallpaper by ID */
    suspend fun getWallpaperById(id: String): Wallpaper? {
        // Try local cache first
        val cached = wallpaperDao.getWallpaperById(id)
        if (cached != null) return cached

        // Try synced wallpapers
        val synced = syncedWallpaperDao.getWallpaperById(id)
        if (synced != null) return synced.toWallpaper()

        // Try Firebase as fallback
        return try {
            withTimeoutOrNull(5_000L) {
                firebaseDataSource.getWallpaperById(id)?.also { wallpaperDao.insertWallpaper(it) }
            }
        } catch (_: Exception) {
            null
        }
    }

    /** Search wallpapers */
    fun searchWallpapers(query: String): Flow<List<Wallpaper>> {
        return wallpaperDao.searchWallpapers(query)
    }

    /** Increment download count */
    suspend fun incrementDownloadCount(wallpaperId: String) {
        firebaseDataSource.incrementDownloadCount(wallpaperId)
    }

    // ==================== CATEGORIES ====================

    /**
     * Get all categories from VIREX Backend
     */
    fun getAllCategories(): Flow<UiState<List<Category>>> = flow {
        emit(UiState.Loading)
        
        try {
            val response = virexBackendApi.getCategories()
            
            if (response.categories.isNotEmpty()) {
                val categories = response.categories.mapIndexed { index, catItem ->
                    Category(
                        id = catItem.id,
                        name = catItem.name,
                        coverUrl = catItem.coverUrl ?: "",
                        wallpaperCount = catItem.count ?: 0,
                        sortOrder = index,
                        isVisible = true
                    )
                }
                android.util.Log.d(TAG, "Categories: ${categories.size} from backend")
                emit(UiState.Success(categories))
            } else {
                // Fallback to local categories
                emitLocalCategories()
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "getAllCategories error: ${e.message}", e)
            emitLocalCategories()
        }
    }.catch { e ->
        android.util.Log.e(TAG, "getAllCategories error", e)
        emit(UiState.Success(getDefaultCategories()))
    }
    
    private suspend fun kotlinx.coroutines.flow.FlowCollector<UiState<List<Category>>>.emitLocalCategories() {
        val synced = syncedWallpaperDao.getAllSyncedWallpapersList()
        
        val categories = SyncCategory.values().mapIndexed { index, syncCat ->
            val wallpapersInCategory = synced.filter { it.category == syncCat }
            Category(
                id = syncCat.name.lowercase(),
                name = syncCat.displayName,
                coverUrl = wallpapersInCategory.firstOrNull()?.thumbnailUrl ?: "",
                wallpaperCount = wallpapersInCategory.size,
                sortOrder = index,
                isVisible = wallpapersInCategory.isNotEmpty()
            )
        }.filter { it.isVisible }

        if (categories.isNotEmpty()) {
            emit(UiState.Success(categories))
        } else {
            emit(UiState.Success(getDefaultCategories()))
        }
    }
    
    private fun getDefaultCategories(): List<Category> {
        return SyncCategory.values().mapIndexed { index, syncCat ->
            Category(
                id = syncCat.name.lowercase(),
                name = syncCat.displayName,
                coverUrl = "",
                wallpaperCount = 0,
                sortOrder = index,
                isVisible = true
            )
        }
    }

    // ==================== FAVORITES ====================

    /** Get favorite wallpapers */
    fun getFavoriteWallpapers(): Flow<List<Wallpaper>> {
        return wallpaperDao.getFavoriteWallpapers()
    }

    /** Check if wallpaper is favorite */
    fun isFavorite(wallpaperId: String): Flow<Boolean> {
        return wallpaperDao.isFavorite(wallpaperId)
    }

    /** Toggle favorite status */
    suspend fun toggleFavorite(wallpaperId: String): Boolean {
        val isFavorite = wallpaperDao.isFavoriteSync(wallpaperId)
        if (isFavorite) {
            wallpaperDao.removeFromFavorites(wallpaperId)
        } else {
            wallpaperDao.addToFavorites(FavoriteWallpaper(wallpaperId))
            firebaseDataSource.incrementLikeCount(wallpaperId)
        }
        return !isFavorite
    }

    /** Get favorites count */
    fun getFavoritesCount(): Flow<Int> {
        return wallpaperDao.getFavoriteCount()
    }

    // ==================== CACHE ====================

    /** Get total cache size */
    fun getCacheSize(): Flow<Long> {
        return wallpaperDao.getTotalCacheSize().map { it ?: 0L }
    }

    /** Clear all cache */
    suspend fun clearCache() {
        wallpaperDao.clearAllCachedWallpapers()
        wallpaperDao.deleteAllWallpapers()
    }
}
