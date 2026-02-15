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
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Wallpaper Repository
 *
 * Single source of truth for wallpaper data. Uses Wallhaven sync as primary data source. VPN
 * recommended for Russia.
 */
@Singleton
class WallpaperRepository
@Inject
constructor(
        private val firebaseDataSource: FirebaseDataSource,
        private val wallpaperDao: WallpaperDao,
        private val syncedWallpaperDao: SyncedWallpaperDao,
        private val preferencesDataStore: PreferencesDataStore
) {

    // ==================== WALLPAPERS ====================

    /**
     * Get all wallpapers from synced data (Wallhaven). Reactive - updates when wallpapers change in
     * database.
     */
    fun getAllWallpapers(): Flow<UiState<List<Wallpaper>>> =
            syncedWallpaperDao
                    .getAllSyncedWallpapers()
                    .map { syncedWallpapers ->
                        val wallpapers = syncedWallpapers.map { it.toWallpaper() }
                        android.util.Log.d(
                                "WallpaperRepo",
                                "getAllWallpapers: ${wallpapers.size} wallpapers"
                        )
                        if (wallpapers.isNotEmpty()) {
                            UiState.Success(wallpapers)
                        } else {
                            UiState.Empty
                        }
                    }
                    .catch { e ->
                        android.util.Log.e("WallpaperRepo", "getAllWallpapers error", e)
                        emit(UiState.Error("Не удалось загрузить обои. Проверьте подключение"))
                    }

    /** Get wallpapers by category. Reactive - updates when wallpapers change in database. */
    fun getWallpapersByCategory(categoryId: String): Flow<UiState<List<Wallpaper>>> {
        android.util.Log.d("WallpaperRepo", "getWallpapersByCategory($categoryId)")

        val syncCategory =
                try {
                    SyncCategory.valueOf(categoryId.uppercase())
                } catch (_: Exception) {
                    SyncCategory.values().find {
                        it.name.equals(categoryId, ignoreCase = true) ||
                                it.displayName.equals(categoryId, ignoreCase = true)
                    }
                }

        return if (syncCategory != null) {
            syncedWallpaperDao
                    .getWallpapersByCategory(syncCategory)
                    .map { syncedList ->
                        val wallpapers = syncedList.map { it.toWallpaper() }
                        android.util.Log.d(
                                "WallpaperRepo",
                                "Category $categoryId: ${wallpapers.size} wallpapers"
                        )
                        if (wallpapers.isNotEmpty()) {
                            UiState.Success(wallpapers)
                        } else {
                            UiState.Empty
                        }
                    }
                    .catch { e ->
                        android.util.Log.e("WallpaperRepo", "getWallpapersByCategory error", e)
                        emit(UiState.Error("Не удалось загрузить обои"))
                    }
        } else {
            // Fallback - filter from all wallpapers
            syncedWallpaperDao
                    .getAllSyncedWallpapers()
                    .map { all ->
                        val wallpapers =
                                all
                                        .filter {
                                            it.category.name.equals(categoryId, ignoreCase = true)
                                        }
                                        .map { it.toWallpaper() }
                        android.util.Log.d(
                                "WallpaperRepo",
                                "Category $categoryId (fallback): ${wallpapers.size} wallpapers"
                        )
                        if (wallpapers.isNotEmpty()) {
                            UiState.Success(wallpapers)
                        } else {
                            UiState.Empty
                        }
                    }
                    .catch { e ->
                        android.util.Log.e("WallpaperRepo", "getWallpapersByCategory error", e)
                        emit(UiState.Error("Не удалось загрузить обои"))
                    }
        }
    }

    /**
     * Get featured wallpapers (random selection). Reactive - updates when wallpapers change in
     * database.
     */
    fun getFeaturedWallpapers(): Flow<UiState<List<Wallpaper>>> =
            syncedWallpaperDao
                    .getAllSyncedWallpapers()
                    .map { synced ->
                        val wallpapers = synced.shuffled().take(30).map { it.toWallpaper() }
                        if (wallpapers.isNotEmpty()) {
                            UiState.Success(wallpapers)
                        } else {
                            UiState.Empty
                        }
                    }
                    .catch { e ->
                        android.util.Log.e("WallpaperRepo", "getFeaturedWallpapers error", e)
                        emit(UiState.Error("Не удалось загрузить обои"))
                    }

    /**
     * Get trending wallpapers (sorted by likes). Reactive - updates when wallpapers change in
     * database.
     */
    fun getTrendingWallpapers(): Flow<UiState<List<Wallpaper>>> =
            syncedWallpaperDao
                    .getAllSyncedWallpapers()
                    .map { synced ->
                        val wallpapers =
                                synced.sortedByDescending { it.likes }.take(30).map {
                                    it.toWallpaper()
                                }
                        if (wallpapers.isNotEmpty()) {
                            UiState.Success(wallpapers)
                        } else {
                            UiState.Empty
                        }
                    }
                    .catch { e ->
                        android.util.Log.e("WallpaperRepo", "getTrendingWallpapers error", e)
                        emit(UiState.Error("Не удалось загрузить обои"))
                    }

    /**
     * Get new wallpapers (sorted by date). Reactive - updates when wallpapers change in database.
     */
    fun getNewWallpapers(): Flow<UiState<List<Wallpaper>>> =
            syncedWallpaperDao
                    .getAllSyncedWallpapers()
                    .map { synced ->
                        val wallpapers =
                                synced.sortedByDescending { it.syncedAt }.take(30).map {
                                    it.toWallpaper()
                                }
                        if (wallpapers.isNotEmpty()) {
                            UiState.Success(wallpapers)
                        } else {
                            UiState.Empty
                        }
                    }
                    .catch { e ->
                        android.util.Log.e("WallpaperRepo", "getNewWallpapers error", e)
                        emit(UiState.Error("Не удалось загрузить обои"))
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
     * Get all categories from synced wallpapers. Reactive - updates when wallpapers change in
     * database.
     */
    fun getAllCategories(): Flow<UiState<List<Category>>> =
            syncedWallpaperDao
                    .getAllSyncedWallpapers()
                    .map { synced ->
                        android.util.Log.d(
                                "WallpaperRepo",
                                "getAllCategories: ${synced.size} wallpapers"
                        )

                        val categories =
                                SyncCategory.values()
                                        .mapIndexed { index, syncCat ->
                                            val wallpapersInCategory =
                                                    synced.filter { it.category == syncCat }
                                            Category(
                                                    id = syncCat.name.lowercase(),
                                                    name = syncCat.displayName,
                                                    coverUrl =
                                                            wallpapersInCategory.firstOrNull()
                                                                    ?.thumbnailUrl
                                                                    ?: "",
                                                    wallpaperCount = wallpapersInCategory.size,
                                                    sortOrder = index,
                                                    isVisible = wallpapersInCategory.isNotEmpty()
                                            )
                                        }
                                        .filter { it.isVisible }

                        if (categories.isNotEmpty()) {
                            UiState.Success(categories)
                        } else {
                            // Return all categories even if empty
                            UiState.Success(
                                    SyncCategory.values().mapIndexed { index, syncCat ->
                                        Category(
                                                id = syncCat.name.lowercase(),
                                                name = syncCat.displayName,
                                                coverUrl = "",
                                                wallpaperCount = 0,
                                                sortOrder = index,
                                                isVisible = true
                                        )
                                    }
                            )
                        }
                    }
                    .catch { e ->
                        android.util.Log.e("WallpaperRepo", "getAllCategories error", e)
                        emit(
                                UiState.Success(
                                        SyncCategory.values().mapIndexed { index, syncCat ->
                                            Category(
                                                    id = syncCat.name.lowercase(),
                                                    name = syncCat.displayName,
                                                    coverUrl = "",
                                                    wallpaperCount = 0,
                                                    sortOrder = index,
                                                    isVisible = true
                                            )
                                        }
                                )
                        )
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
