package com.virex.wallpapers.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.virex.wallpapers.data.model.SyncCategory
import com.virex.wallpapers.data.model.SyncStatus
import com.virex.wallpapers.data.model.SyncedWallpaper
import com.virex.wallpapers.data.model.WallpaperSource
import kotlinx.coroutines.flow.Flow

/** DAO for synced wallpapers from external APIs */
@Dao
interface SyncedWallpaperDao {

    // ==================== SYNCED WALLPAPERS ====================

    /** Get all synced wallpapers, newest first */
    @Query("SELECT * FROM synced_wallpapers ORDER BY syncedAt DESC")
    fun getAllSyncedWallpapers(): Flow<List<SyncedWallpaper>>
    
    /** Get all synced wallpapers as list (suspend) */
    @Query("SELECT * FROM synced_wallpapers ORDER BY syncedAt DESC")
    suspend fun getAllSyncedWallpapersList(): List<SyncedWallpaper>

    /** Get synced wallpapers by category */
    @Query("SELECT * FROM synced_wallpapers WHERE category = :category ORDER BY syncedAt DESC")
    fun getWallpapersByCategory(category: SyncCategory): Flow<List<SyncedWallpaper>>
    
    /** Get synced wallpapers by category as list (suspend) */
    @Query("SELECT * FROM synced_wallpapers WHERE category = :category ORDER BY syncedAt DESC")
    suspend fun getWallpapersByCategoryList(category: SyncCategory): List<SyncedWallpaper>

    /** Get new wallpapers (synced within last 7 days) */
    @Query(
            """
        SELECT * FROM synced_wallpapers 
        WHERE syncedAt > :since 
        ORDER BY syncedAt DESC 
        LIMIT :limit
    """
    )
    fun getNewWallpapers(since: Long, limit: Int = 50): Flow<List<SyncedWallpaper>>

    /** Get wallpapers by source */
    @Query("SELECT * FROM synced_wallpapers WHERE source = :source ORDER BY syncedAt DESC")
    fun getWallpapersBySource(source: WallpaperSource): Flow<List<SyncedWallpaper>>

    /** Get a single wallpaper by ID */
    @Query("SELECT * FROM synced_wallpapers WHERE id = :id")
    suspend fun getWallpaperById(id: String): SyncedWallpaper?

    /** Check if wallpaper exists by source ID (for deduplication) */
    @Query(
            "SELECT EXISTS(SELECT 1 FROM synced_wallpapers WHERE sourceId = :sourceId AND source = :source)"
    )
    suspend fun exists(sourceId: String, source: WallpaperSource): Boolean

    /** Get existing source IDs for batch deduplication */
    @Query("SELECT sourceId FROM synced_wallpapers WHERE source = :source")
    suspend fun getExistingSourceIds(source: WallpaperSource): List<String>

    /** Get ALL wallpaper IDs for batch deduplication across all sources */
    @Query("SELECT id FROM synced_wallpapers") suspend fun getAllWallpaperIds(): List<String>

    /** Insert wallpapers, skip duplicates */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWallpapers(wallpapers: List<SyncedWallpaper>): List<Long>

    /** Insert single wallpaper */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWallpaper(wallpaper: SyncedWallpaper): Long

    /** Update wallpaper */
    @Update suspend fun updateWallpaper(wallpaper: SyncedWallpaper)

    /** Mark wallpaper as viewed */
    @Query("UPDATE synced_wallpapers SET viewed = 1 WHERE id = :id")
    suspend fun markAsViewed(id: String)

    /** Update cache path */
    @Query("UPDATE synced_wallpapers SET localCachePath = :path, isCached = 1 WHERE id = :id")
    suspend fun updateCachePath(id: String, path: String)

    /** Delete wallpaper */
    @Query("DELETE FROM synced_wallpapers WHERE id = :id") suspend fun deleteWallpaper(id: String)

    /** Delete old wallpapers (older than X days) */
    @Query("DELETE FROM synced_wallpapers WHERE syncedAt < :before AND isCached = 0")
    suspend fun deleteOldWallpapers(before: Long): Int

    /** Get count of wallpapers */
    @Query("SELECT COUNT(*) FROM synced_wallpapers") fun getWallpaperCount(): Flow<Int>

    /** Get count by category */
    @Query("SELECT COUNT(*) FROM synced_wallpapers WHERE category = :category")
    suspend fun getCountByCategory(category: SyncCategory): Int

    /** Search wallpapers */
    @Query(
            """
        SELECT * FROM synced_wallpapers 
        WHERE description LIKE '%' || :query || '%' 
           OR tags LIKE '%' || :query || '%'
           OR photographerName LIKE '%' || :query || '%'
        ORDER BY syncedAt DESC
    """
    )
    fun searchWallpapers(query: String): Flow<List<SyncedWallpaper>>

    /** Get unviewed count (for badge) */
    @Query("SELECT COUNT(*) FROM synced_wallpapers WHERE viewed = 0")
    fun getUnviewedCount(): Flow<Int>

    /** Clear all synced wallpapers */
    @Query("DELETE FROM synced_wallpapers") suspend fun clearAll()

    // ==================== SYNC STATUS ====================

    /** Get current sync status */
    @Query("SELECT * FROM sync_status WHERE id = 'default'") fun getSyncStatus(): Flow<SyncStatus?>

    /** Get sync status synchronously */
    @Query("SELECT * FROM sync_status WHERE id = 'default'")
    suspend fun getSyncStatusSync(): SyncStatus?

    /** Insert or update sync status */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSyncStatus(status: SyncStatus)

    /** Update last sync time */
    @Query(
            """
        UPDATE sync_status 
        SET lastSyncAt = :timestamp, 
            lastSyncSource = :source, 
            lastSyncCount = :count,
            isSyncing = 0,
            lastError = null,
            totalSynced = totalSynced + :count
        WHERE id = 'default'
    """
    )
    suspend fun updateLastSync(timestamp: Long, source: String, count: Int)

    /** Set syncing state */
    @Query("UPDATE sync_status SET isSyncing = :syncing WHERE id = 'default'")
    suspend fun setSyncing(syncing: Boolean)

    /** Set sync error */
    @Query("UPDATE sync_status SET isSyncing = 0, lastError = :error WHERE id = 'default'")
    suspend fun setSyncError(error: String)

    /** Update pagination state */
    @Query(
            """
        UPDATE sync_status 
        SET unsplashPage = :unsplashPage, pexelsPage = :pexelsPage 
        WHERE id = 'default'
    """
    )
    suspend fun updatePagination(unsplashPage: Int, pexelsPage: Int)
}
