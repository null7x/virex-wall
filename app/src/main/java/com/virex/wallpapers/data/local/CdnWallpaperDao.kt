package com.virex.wallpapers.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.virex.wallpapers.data.model.CdnWallpaper
import kotlinx.coroutines.flow.Flow

/**
 * DAO for CDN Wallpapers
 *
 * Provides cache access for wallpapers from GitHub CDN, Wallhaven, Picsum.
 */
@Dao
interface CdnWallpaperDao {

    // ==================== Queries ====================

    /** Get all cached CDN wallpapers ordered by newest first */
    @Query("SELECT * FROM cdn_wallpapers ORDER BY cachedAt DESC")
    fun getAllWallpapers(): Flow<List<CdnWallpaper>>

    /** Get all cached CDN wallpapers (suspend, one-shot) */
    @Query("SELECT * FROM cdn_wallpapers ORDER BY cachedAt DESC")
    suspend fun getAllWallpapersOnce(): List<CdnWallpaper>

    /** Get wallpapers by source */
    @Query("SELECT * FROM cdn_wallpapers WHERE source = :source ORDER BY cachedAt DESC")
    fun getWallpapersBySource(source: String): Flow<List<CdnWallpaper>>

    /** Get free (non-PRO) wallpapers */
    @Query("SELECT * FROM cdn_wallpapers WHERE isPro = 0 ORDER BY cachedAt DESC")
    fun getFreeWallpapers(): Flow<List<CdnWallpaper>>

    /** Get PRO wallpapers */
    @Query("SELECT * FROM cdn_wallpapers WHERE isPro = 1 ORDER BY cachedAt DESC")
    fun getProWallpapers(): Flow<List<CdnWallpaper>>

    /** Get wallpaper by ID */
    @Query("SELECT * FROM cdn_wallpapers WHERE id = :id")
    suspend fun getWallpaperById(id: String): CdnWallpaper?

    /** Check if a wallpaper exists by remoteId and source */
    @Query(
            "SELECT EXISTS(SELECT 1 FROM cdn_wallpapers WHERE remoteId = :remoteId AND source = :source)"
    )
    suspend fun exists(remoteId: String, source: String): Boolean

    /** Get count of cached wallpapers */
    @Query("SELECT COUNT(*) FROM cdn_wallpapers") suspend fun getCount(): Int

    /** Search wallpapers by tags or author */
    @Query(
            "SELECT * FROM cdn_wallpapers WHERE tags LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' ORDER BY cachedAt DESC"
    )
    fun searchWallpapers(query: String): Flow<List<CdnWallpaper>>

    /** Get wallpapers by category */
    @Query("SELECT * FROM cdn_wallpapers WHERE category = :category ORDER BY cachedAt DESC")
    fun getWallpapersByCategory(category: String): Flow<List<CdnWallpaper>>
    
    /** Get wallpapers by category (one-shot) */
    @Query("SELECT * FROM cdn_wallpapers WHERE category = :category ORDER BY cachedAt DESC")
    suspend fun getWallpapersByCategoryOnce(category: String): List<CdnWallpaper>

    // ==================== Inserts ====================

    /** Insert or replace wallpapers */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallpapers(wallpapers: List<CdnWallpaper>)

    /** Insert or replace a single wallpaper */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallpaper(wallpaper: CdnWallpaper)

    // ==================== Deletes ====================

    /** Clear all CDN wallpapers */
    @Query("DELETE FROM cdn_wallpapers") suspend fun clearAll()

    /** Clear wallpapers from a specific source */
    @Query("DELETE FROM cdn_wallpapers WHERE source = :source")
    suspend fun clearBySource(source: String)

    /** Delete old wallpapers (cleanup) */
    @Query("DELETE FROM cdn_wallpapers WHERE cachedAt < :before")
    suspend fun deleteOldWallpapers(before: Long)
}
