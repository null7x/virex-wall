package com.virex.wallpapers.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.virex.wallpapers.data.model.CachedWallpaper
import com.virex.wallpapers.data.model.DownloadHistory
import com.virex.wallpapers.data.model.FavoriteWallpaper
import com.virex.wallpapers.data.model.Wallpaper
import kotlinx.coroutines.flow.Flow

/** Wallpaper DAO for Room database operations */
@Dao
interface WallpaperDao {

    // ==================== WALLPAPERS ====================

    @Query("SELECT * FROM wallpapers ORDER BY createdAt DESC")
    fun getAllWallpapers(): Flow<List<Wallpaper>>

    @Query("SELECT * FROM wallpapers WHERE id = :id")
    suspend fun getWallpaperById(id: String): Wallpaper?

    @Query("SELECT * FROM wallpapers WHERE categoryId = :categoryId ORDER BY createdAt DESC")
    fun getWallpapersByCategory(categoryId: String): Flow<List<Wallpaper>>

    @Query("SELECT * FROM wallpapers WHERE isFeatured = 1 ORDER BY createdAt DESC LIMIT :limit")
    fun getFeaturedWallpapers(limit: Int = 10): Flow<List<Wallpaper>>

    @Query("SELECT * FROM wallpapers WHERE isTrending = 1 ORDER BY downloads DESC LIMIT :limit")
    fun getTrendingWallpapers(limit: Int = 10): Flow<List<Wallpaper>>

    @Query("SELECT * FROM wallpapers ORDER BY createdAt DESC LIMIT :limit")
    fun getNewWallpapers(limit: Int = 20): Flow<List<Wallpaper>>

    @Query("SELECT * FROM wallpapers WHERE isPremium = 0 ORDER BY createdAt DESC")
    fun getFreeWallpapers(): Flow<List<Wallpaper>>

    @Query(
            "SELECT * FROM wallpapers WHERE title LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%'"
    )
    fun searchWallpapers(query: String): Flow<List<Wallpaper>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallpaper(wallpaper: Wallpaper)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallpapers(wallpapers: List<Wallpaper>)

    @Update suspend fun updateWallpaper(wallpaper: Wallpaper)

    @Delete suspend fun deleteWallpaper(wallpaper: Wallpaper)

    @Query("DELETE FROM wallpapers") suspend fun deleteAllWallpapers()

    // ==================== FAVORITES ====================

    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteWallpaper>>

    @Query(
            "SELECT * FROM wallpapers WHERE id IN (SELECT wallpaperId FROM favorites) ORDER BY createdAt DESC"
    )
    fun getFavoriteWallpapers(): Flow<List<Wallpaper>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE wallpaperId = :wallpaperId)")
    fun isFavorite(wallpaperId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE wallpaperId = :wallpaperId)")
    suspend fun isFavoriteSync(wallpaperId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToFavorites(favorite: FavoriteWallpaper)

    @Query("DELETE FROM favorites WHERE wallpaperId = :wallpaperId")
    suspend fun removeFromFavorites(wallpaperId: String)

    @Query("SELECT COUNT(*) FROM favorites") fun getFavoriteCount(): Flow<Int>

    // ==================== CACHED WALLPAPERS ====================

    @Query("SELECT * FROM cached_wallpapers ORDER BY cachedAt DESC")
    fun getAllCachedWallpapers(): Flow<List<CachedWallpaper>>

    @Query("SELECT * FROM cached_wallpapers WHERE wallpaperId = :wallpaperId")
    suspend fun getCachedWallpaper(wallpaperId: String): CachedWallpaper?

    @Query("SELECT EXISTS(SELECT 1 FROM cached_wallpapers WHERE wallpaperId = :wallpaperId)")
    fun isCached(wallpaperId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheWallpaper(cached: CachedWallpaper)

    @Query("DELETE FROM cached_wallpapers WHERE wallpaperId = :wallpaperId")
    suspend fun removeCachedWallpaper(wallpaperId: String)

    @Query("DELETE FROM cached_wallpapers") suspend fun clearAllCachedWallpapers()

    @Query("SELECT SUM(fileSize) FROM cached_wallpapers") fun getTotalCacheSize(): Flow<Long?>

    // ==================== DOWNLOAD HISTORY ====================

    @Query("SELECT * FROM download_history ORDER BY downloadedAt DESC LIMIT :limit")
    fun getDownloadHistory(limit: Int = 50): Flow<List<DownloadHistory>>

    @Insert suspend fun addToDownloadHistory(history: DownloadHistory)

    @Query("DELETE FROM download_history") suspend fun clearDownloadHistory()

    @Query("SELECT COUNT(*) FROM download_history") fun getDownloadCount(): Flow<Int>
}
