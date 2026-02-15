package com.virex.wallpapers.data.repository

import android.util.Log
import com.virex.wallpapers.BuildConfig
import com.virex.wallpapers.data.local.SyncedWallpaperDao
import com.virex.wallpapers.data.model.SyncCategory
import com.virex.wallpapers.data.model.SyncStatus
import com.virex.wallpapers.data.model.SyncedWallpaper
import com.virex.wallpapers.data.model.WallpaperSource
import com.virex.wallpapers.data.remote.api.PexelsApi
import com.virex.wallpapers.data.remote.api.PicsumApi
import com.virex.wallpapers.data.remote.api.UnsplashApi
import com.virex.wallpapers.data.remote.api.WallhavenApi
import com.virex.wallpapers.data.remote.model.PexelsPhoto
import com.virex.wallpapers.data.remote.model.PicsumPhoto
import com.virex.wallpapers.data.remote.model.UnsplashPhoto
import com.virex.wallpapers.data.remote.model.WallhavenWallpaper
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/**
 * Repository for syncing wallpapers from external APIs
 *
 * FALLBACK CHAIN (works in Russia):
 * 1. Wallhaven - PRIMARY (no API key needed, works globally)
 * 2. Picsum - FALLBACK (no API key needed, works globally)
 * 3. Unsplash - OPTIONAL (needs API key, may be blocked)
 * 4. Pexels - OPTIONAL (needs API key, may be blocked)
 */
@Singleton
class WallpaperSyncRepository
@Inject
constructor(
        private val unsplashApi: UnsplashApi,
        private val pexelsApi: PexelsApi,
        private val wallhavenApi: WallhavenApi,
        private val picsumApi: PicsumApi,
        private val syncedWallpaperDao: SyncedWallpaperDao
) {
    private val syncMutex = Mutex()

    companion object {
        private const val TAG = "WallpaperSyncRepository"

        // Sync settings
        private const val WALLPAPERS_PER_CATEGORY = 30
        private const val MAX_WALLPAPERS_PER_SYNC = 120
        private const val OLD_WALLPAPER_DAYS = 30

        // Retry settings
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY_MS = 1000L
    }

    // ==================== PUBLIC FLOWS ====================

    fun getAllSyncedWallpapers(): Flow<List<SyncedWallpaper>> =
            syncedWallpaperDao.getAllSyncedWallpapers()

    fun getWallpapersByCategory(category: SyncCategory): Flow<List<SyncedWallpaper>> =
            syncedWallpaperDao.getWallpapersByCategory(category)

    fun getNewWallpapers(limit: Int = 50): Flow<List<SyncedWallpaper>> {
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        return syncedWallpaperDao.getNewWallpapers(sevenDaysAgo, limit)
    }

    fun getSyncStatus(): Flow<SyncStatus?> = syncedWallpaperDao.getSyncStatus()

    fun getUnviewedCount(): Flow<Int> = syncedWallpaperDao.getUnviewedCount()

    fun searchWallpapers(query: String): Flow<List<SyncedWallpaper>> =
            syncedWallpaperDao.searchWallpapers(query)

    // ==================== SYNC OPERATIONS ====================

    suspend fun performSync(): SyncResult =
            withContext(Dispatchers.IO) {
                if (!syncMutex.tryLock()) {
                    Log.d(TAG, "Sync already in progress, skipping duplicate request")
                    return@withContext SyncResult.Success(0)
                }

                try {
                    Log.d(TAG, "Starting wallpaper sync with fallback chain")

                    ensureSyncStatusExists()
                    syncedWallpaperDao.setSyncing(true)

                    var totalNew = 0
                    val errors = mutableListOf<String>()
                    val successfulSources = mutableListOf<String>()
                    var hadSuccessfulSource = false

                    val existingIds = syncedWallpaperDao.getAllWallpaperIds().toSet()
                    Log.d(TAG, "Existing wallpapers: ${existingIds.size}")

                    val wallhavenResult = syncFromWallhaven(existingIds)
                    if (wallhavenResult.isSuccess) {
                        hadSuccessfulSource = true
                        totalNew += wallhavenResult.count
                        successfulSources.add("wallhaven")
                        Log.d(TAG, "Wallhaven: ${wallhavenResult.count} inserted")
                    } else {
                        errors.add("Wallhaven: ${wallhavenResult.error ?: "unknown"}")
                    }

                    delay(300)

                    val picsumResult = syncFromPicsum(existingIds)
                    if (picsumResult.isSuccess) {
                        hadSuccessfulSource = true
                        totalNew += picsumResult.count
                        successfulSources.add("picsum")
                        Log.d(TAG, "Picsum: ${picsumResult.count} inserted")
                    } else {
                        errors.add("Picsum: ${picsumResult.error ?: "unknown"}")
                    }

                    delay(300)

                    val unsplashKey = getUnsplashApiKey()
                    if (unsplashKey.isNotBlank()) {
                        val unsplashResult = syncFromUnsplash(existingIds)
                        if (unsplashResult.isSuccess) {
                            hadSuccessfulSource = true
                            totalNew += unsplashResult.count
                            successfulSources.add("unsplash")
                            Log.d(TAG, "Unsplash: ${unsplashResult.count} inserted")
                        } else {
                            errors.add("Unsplash: ${unsplashResult.error ?: "unknown"}")
                        }
                        delay(300)
                    } else {
                        Log.d(TAG, "Unsplash skipped (no API key)")
                    }

                    val pexelsKey = getPexelsApiKey()
                    if (pexelsKey.isNotBlank()) {
                        val pexelsResult = syncFromPexels(existingIds)
                        if (pexelsResult.isSuccess) {
                            hadSuccessfulSource = true
                            totalNew += pexelsResult.count
                            successfulSources.add("pexels")
                            Log.d(TAG, "Pexels: ${pexelsResult.count} inserted")
                        } else {
                            errors.add("Pexels: ${pexelsResult.error ?: "unknown"}")
                        }
                    } else {
                        Log.d(TAG, "Pexels skipped (no API key)")
                    }

                    val deleted = cleanupOldWallpapers()
                    if (deleted > 0) {
                        Log.d(TAG, "Cleaned up $deleted old wallpapers")
                    }

                    if (hadSuccessfulSource) {
                        syncedWallpaperDao.updateLastSync(
                                timestamp = System.currentTimeMillis(),
                                source = successfulSources.joinToString(","),
                                count = totalNew
                        )
                        return@withContext SyncResult.Success(totalNew)
                    }

                    val errorMsg =
                            if (errors.isNotEmpty()) {
                                "All sources failed: ${errors.joinToString("; ")}"
                            } else {
                                "All sources failed"
                            }
                    syncedWallpaperDao.setSyncError(errorMsg)
                    return@withContext SyncResult.Error(errorMsg)
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Log.e(TAG, "Sync failed", e)
                    val errorMsg = e.message ?: "Sync failed"
                    syncedWallpaperDao.setSyncError(errorMsg)
                    return@withContext SyncResult.Error(errorMsg)
                } finally {
                    syncMutex.unlock()
                    syncedWallpaperDao.setSyncing(false)
                }
            }

    private suspend fun syncFromUnsplash(existingIds: Set<String>): SourceSyncResult {
        val apiKey = getUnsplashApiKey()
        if (apiKey.isBlank()) {
            return SourceSyncResult(false, 0, "API key not configured")
        }

        val allPhotos = mutableListOf<Pair<UnsplashPhoto, SyncCategory>>()
        var hasSuccessfulResponse = false

        try {
            for (category in SyncCategory.values()) {
                val query = category.searchTerms.random()
                val response = retryWithBackoff {
                    unsplashApi.searchPhotos(
                            clientId = "Client-ID $apiKey",
                            query = query,
                            perPage = WALLPAPERS_PER_CATEGORY,
                            orientation = "portrait",
                            color = if (category == SyncCategory.AMOLED) "black" else null
                    )
                }

                if (response.isSuccessful) {
                    hasSuccessfulResponse = true
                    response.body()?.results?.let { photos ->
                        allPhotos.addAll(photos.map { it to category })
                    }
                } else {
                    Log.w(TAG, "Unsplash search failed: ${response.code()}")
                }
                delay(200)
            }

            if (!hasSuccessfulResponse) {
                return SourceSyncResult(false, 0, "No successful responses")
            }

            val newWallpapers =
                    allPhotos
                            .filter { "unsplash_${it.first.id}" !in existingIds }
                            .map { (photo, category) -> photo.toSyncedWallpaper(category) }
                            .distinctBy { it.sourceId }
                            .take(MAX_WALLPAPERS_PER_SYNC)

            val insertedCount = insertNewWallpapers(newWallpapers)
            return SourceSyncResult(true, insertedCount)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Unsplash sync error", e)
            return SourceSyncResult(false, 0, e.message)
        }
    }

    private suspend fun syncFromPexels(existingIds: Set<String>): SourceSyncResult {
        val apiKey = getPexelsApiKey()
        if (apiKey.isBlank()) {
            return SourceSyncResult(false, 0, "API key not configured")
        }

        val allPhotos = mutableListOf<Pair<PexelsPhoto, SyncCategory>>()
        var hasSuccessfulResponse = false

        try {
            for (category in SyncCategory.values()) {
                val query = category.searchTerms.random()
                val response = retryWithBackoff {
                    pexelsApi.searchPhotos(
                            apiKey = apiKey,
                            query = query,
                            perPage = WALLPAPERS_PER_CATEGORY,
                            orientation = "portrait",
                            color = if (category == SyncCategory.AMOLED) "black" else null
                    )
                }

                if (response.isSuccessful) {
                    hasSuccessfulResponse = true
                    response.body()?.photos?.let { photos ->
                        allPhotos.addAll(photos.map { it to category })
                    }
                } else {
                    Log.w(TAG, "Pexels search failed: ${response.code()}")
                }
                delay(200)
            }

            if (!hasSuccessfulResponse) {
                return SourceSyncResult(false, 0, "No successful responses")
            }

            val newWallpapers =
                    allPhotos
                            .filter { "pexels_${it.first.id}" !in existingIds }
                            .map { (photo, category) -> photo.toSyncedWallpaper(category) }
                            .distinctBy { it.sourceId }
                            .take(MAX_WALLPAPERS_PER_SYNC)

            val insertedCount = insertNewWallpapers(newWallpapers)
            return SourceSyncResult(true, insertedCount)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Pexels sync error", e)
            return SourceSyncResult(false, 0, e.message)
        }
    }

    private suspend fun syncFromWallhaven(existingIds: Set<String>): SourceSyncResult {
        val allWallpapers = mutableListOf<Pair<WallhavenWallpaper, SyncCategory>>()
        var successfulRequests = 0

        try {
            // Sync ALL categories from SyncCategory enum
            for (category in SyncCategory.values()) {
                // Skip NEW - will be handled separately with date_added sorting
                if (category == SyncCategory.NEW) continue

                val query = category.searchTerms.random()
                try {
                    val response = retryWithBackoff {
                        wallhavenApi.searchWallpapers(
                                query = query,
                                categories = "100",
                                purity = "100",
                                sorting = "toplist",
                                ratios = "portrait",
                                atleast = "1080x1920",
                                page = 1
                        )
                    }
                    successfulRequests++
                    response.data.forEach { wallpaper -> allWallpapers.add(wallpaper to category) }
                    Log.d(TAG, "Wallhaven ${category.name}: ${response.data.size} wallpapers")
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Log.w(TAG, "Wallhaven ${category.name} query failed: ${e.message}")
                }
                delay(150)
            }

            // Fetch NEW wallpapers (sorted by date)
            try {
                val newResponse = retryWithBackoff {
                    wallhavenApi.searchWallpapers(
                            query = "",
                            categories = "100",
                            purity = "100",
                            sorting = "date_added",
                            ratios = "portrait",
                            atleast = "1080x1920",
                            page = 1
                    )
                }
                successfulRequests++
                newResponse.data.forEach { wallpaper ->
                    allWallpapers.add(wallpaper to SyncCategory.NEW)
                }
                Log.d(TAG, "Wallhaven NEW: ${newResponse.data.size} wallpapers")
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.w(TAG, "Wallhaven NEW query failed: ${e.message}")
            }

            if (successfulRequests == 0) {
                return SourceSyncResult(false, 0, "No successful responses")
            }

            val newWallpapers =
                    allWallpapers
                            .filter { "wallhaven_${it.first.id}" !in existingIds }
                            .map { (wallpaper, category) -> wallpaper.toSyncedWallpaper(category) }
                            .distinctBy { it.sourceId }
                            .take(MAX_WALLPAPERS_PER_SYNC)

            val insertedCount = insertNewWallpapers(newWallpapers)
            return SourceSyncResult(true, insertedCount)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Wallhaven sync error", e)
            return SourceSyncResult(false, 0, e.message)
        }
    }

    private suspend fun syncFromPicsum(existingIds: Set<String>): SourceSyncResult {
        val allPhotos = mutableListOf<PicsumPhoto>()
        var successfulPages = 0

        try {
            for (page in 1..4) {
                try {
                    val photos = retryWithBackoff { picsumApi.getPhotos(page = page, limit = 30) }
                    successfulPages++
                    allPhotos.addAll(photos)
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Log.w(TAG, "Picsum page $page failed: ${e.message}")
                }
                delay(100)
            }

            if (successfulPages == 0) {
                return SourceSyncResult(false, 0, "No successful responses")
            }

            // Picsum doesn't support category search, assign all to NATURE (generic photos)
            val newWallpapers =
                    allPhotos
                            .filter { "picsum_${it.id}" !in existingIds }
                            .map { photo -> photo.toSyncedWallpaper(SyncCategory.NATURE) }
                            .distinctBy { it.sourceId }
                            .take(MAX_WALLPAPERS_PER_SYNC)

            val insertedCount = insertNewWallpapers(newWallpapers)
            return SourceSyncResult(true, insertedCount)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Picsum sync error", e)
            return SourceSyncResult(false, 0, e.message)
        }
    }

    // ==================== HELPER FUNCTIONS ====================

    private suspend fun ensureSyncStatusExists() {
        if (syncedWallpaperDao.getSyncStatusSync() == null) {
            syncedWallpaperDao.updateSyncStatus(SyncStatus())
        }
    }

    private suspend fun cleanupOldWallpapers(): Int {
        val cutoff = System.currentTimeMillis() - (OLD_WALLPAPER_DAYS * 24 * 60 * 60 * 1000L)
        return syncedWallpaperDao.deleteOldWallpapers(cutoff)
    }

    private suspend fun insertNewWallpapers(wallpapers: List<SyncedWallpaper>): Int {
        if (wallpapers.isEmpty()) return 0
        return syncedWallpaperDao.insertWallpapers(wallpapers).count { it != -1L }
    }

    private fun getUnsplashApiKey(): String {
        val key = BuildConfig.UNSPLASH_ACCESS_KEY
        Log.d(TAG, "Unsplash API key length: ${key.length}, empty: ${key.isBlank()}")
        return key
    }

    private fun getPexelsApiKey(): String {
        val key = BuildConfig.PEXELS_API_KEY
        Log.d(TAG, "Pexels API key length: ${key.length}, empty: ${key.isBlank()}")
        return key
    }

    private suspend fun <T> retryWithBackoff(
            maxRetries: Int = MAX_RETRIES,
            initialDelay: Long = RETRY_DELAY_MS,
            block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        repeat(maxRetries - 1) { attempt ->
            try {
                return block()
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                if (!shouldRetry(error)) throw error
                Log.w(TAG, "Attempt ${attempt + 1} failed, retrying in ${currentDelay}ms")
                delay(currentDelay)
                currentDelay *= 2
            }
        }
        return block()
    }

    private fun shouldRetry(error: Exception): Boolean =
            when (error) {
                is IOException -> true
                is HttpException -> {
                    val code = error.code()
                    code == 429 || code >= 500
                }
                else -> false
            }

    suspend fun markAsViewed(id: String) {
        syncedWallpaperDao.markAsViewed(id)
    }

    suspend fun trackUnsplashDownload(photoId: String) {
        val apiKey = getUnsplashApiKey()
        if (apiKey.isNotBlank()) {
            try {
                unsplashApi.trackDownload("Client-ID $apiKey", photoId)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Failed to track download", e)
            }
        }
    }

    suspend fun getWallpaperById(id: String): SyncedWallpaper? {
        return syncedWallpaperDao.getWallpaperById(id)
    }

    suspend fun clearAllSyncedWallpapers() {
        syncedWallpaperDao.clearAll()
    }
}

// ==================== EXTENSION FUNCTIONS ====================

private fun UnsplashPhoto.toSyncedWallpaper(category: SyncCategory): SyncedWallpaper {
    return SyncedWallpaper(
            id = "unsplash_$id",
            sourceId = id,
            source = WallpaperSource.UNSPLASH,
            thumbnailUrl = urls.small,
            fullUrl = urls.regular,
            previewUrl = urls.regular,
            originalUrl = urls.full,
            width = width,
            height = height,
            color = color,
            blurHash = blurHash,
            description = altDescription ?: description,
            photographerName = user.name,
            photographerUrl = user.links?.html,
            sourceUrl = links.html,
            category = category,
            tags = tags?.mapNotNull { it.title } ?: emptyList(),
            likes = likes,
            createdAt = parseCreatedAt(createdAt),
            syncedAt = System.currentTimeMillis()
    )
}

private fun PexelsPhoto.toSyncedWallpaper(category: SyncCategory): SyncedWallpaper {
    return SyncedWallpaper(
            id = "pexels_$id",
            sourceId = id.toString(),
            source = WallpaperSource.PEXELS,
            thumbnailUrl = src.medium,
            fullUrl = src.large2x,
            previewUrl = src.large,
            originalUrl = src.original,
            width = width,
            height = height,
            color = avgColor,
            blurHash = null,
            description = alt,
            photographerName = photographer,
            photographerUrl = photographerUrl,
            sourceUrl = url,
            category = category,
            tags = emptyList(),
            likes = 0,
            createdAt = null,
            syncedAt = System.currentTimeMillis()
    )
}

private fun WallhavenWallpaper.toSyncedWallpaper(category: SyncCategory): SyncedWallpaper {
    return SyncedWallpaper(
            id = "wallhaven_$id",
            sourceId = id,
            source = WallpaperSource.WALLHAVEN,
            thumbnailUrl = thumbs?.small ?: thumbs?.large ?: path,
            fullUrl = path,
            previewUrl = thumbs?.large ?: path,
            originalUrl = path,
            width = dimensionX,
            height = dimensionY,
            color = colors.firstOrNull(),
            blurHash = null,
            description = "Wallhaven: $resolution",
            photographerName = "Wallhaven Community",
            photographerUrl = url,
            sourceUrl = url,
            category = category,
            tags = tags?.map { it.name } ?: emptyList(),
            likes = favorites,
            createdAt = parseWallhavenDate(createdAt),
            syncedAt = System.currentTimeMillis()
    )
}

private fun PicsumPhoto.toSyncedWallpaper(category: SyncCategory): SyncedWallpaper {
    return SyncedWallpaper(
            id = "picsum_$id",
            sourceId = id,
            source = WallpaperSource.PICSUM,
            thumbnailUrl = getThumbnailUrl(),
            fullUrl = getFullUrl(),
            previewUrl = getResizedUrl(800, 1400),
            originalUrl = downloadUrl,
            width = width,
            height = height,
            color = null,
            blurHash = null,
            description = "Photo by $author",
            photographerName = author,
            photographerUrl = url,
            sourceUrl = url,
            category = category,
            tags = emptyList(),
            likes = 0,
            createdAt = null,
            syncedAt = System.currentTimeMillis()
    )
}

private fun parseCreatedAt(dateString: String?): Long? {
    if (dateString == null) return null
    return try {
        java.time.Instant.parse(dateString).toEpochMilli()
    } catch (_: Exception) {
        null
    }
}

private fun parseWallhavenDate(dateString: String?): Long? {
    if (dateString.isNullOrBlank()) return null
    return try {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val localDateTime = java.time.LocalDateTime.parse(dateString, formatter)
        localDateTime.atZone(java.time.ZoneId.of("UTC")).toInstant().toEpochMilli()
    } catch (_: Exception) {
        null
    }
}

sealed class SyncResult {
    data class Success(val newCount: Int) : SyncResult()

    data class Error(val message: String) : SyncResult()

    val isSuccess: Boolean
        get() = this is Success
}

private data class SourceSyncResult(
        val isSuccess: Boolean,
        val count: Int,
        val error: String? = null
)
