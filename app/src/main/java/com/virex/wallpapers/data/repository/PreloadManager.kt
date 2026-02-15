package com.virex.wallpapers.data.repository

import android.content.Context
import android.util.Log
import com.virex.wallpapers.data.local.SyncedWallpaperDao
import com.virex.wallpapers.data.model.SyncCategory
import com.virex.wallpapers.data.model.SyncedWallpaper
import com.virex.wallpapers.data.model.WallpaperSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray

/**
 * Preload Manager
 *
 * Loads bundled wallpapers from assets/preload.json on first install. Ensures the app always has
 * content to display immediately.
 */
@Singleton
class PreloadManager
@Inject
constructor(
        @ApplicationContext private val context: Context,
        private val syncedWallpaperDao: SyncedWallpaperDao
) {
    companion object {
        private const val TAG = "PreloadManager"
        private const val PRELOAD_FILE = "preload.json"
    }

    /** Load preload data if database is empty. Should be called on app startup. */
    suspend fun loadPreloadDataIfEmpty() =
            withContext(Dispatchers.IO) {
                try {
                    val count = syncedWallpaperDao.getWallpaperCount().first()
                    if (count > 0) {
                        Log.d(TAG, "Database has $count wallpapers, skipping preload")
                        return@withContext
                    }

                    Log.d(TAG, "Database empty, loading preload data...")
                    loadPreloadData()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to check/load preload data", e)
                }
            }

    /** Force load preload data (for testing or reset) */
    suspend fun loadPreloadData() =
            withContext(Dispatchers.IO) {
                try {
                    val jsonString =
                            context.assets.open(PRELOAD_FILE).bufferedReader().use { it.readText() }
                    val jsonArray = JSONArray(jsonString)

                    val wallpapers = mutableListOf<SyncedWallpaper>()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)

                        val category =
                                try {
                                    SyncCategory.valueOf(obj.getString("category"))
                                } catch (e: Exception) {
                                    SyncCategory.ABSTRACT // Default fallback
                                }

                        val source =
                                try {
                                    WallpaperSource.valueOf(obj.getString("source"))
                                } catch (e: Exception) {
                                    WallpaperSource.GITHUB_CDN
                                }

                        val wallpaper =
                                SyncedWallpaper(
                                        id = obj.getString("id"),
                                        sourceId = obj.getString("sourceId"),
                                        source = source,
                                        thumbnailUrl = obj.getString("thumbnailUrl"),
                                        fullUrl = obj.getString("fullUrl"),
                                        previewUrl = obj.getString("previewUrl"),
                                        originalUrl = obj.getString("originalUrl"),
                                        width = obj.optInt("width", 1080),
                                        height = obj.optInt("height", 1920),
                                        color =
                                                if (obj.has("color")) obj.getString("color")
                                                else null,
                                        blurHash =
                                                if (obj.has("blurHash")) obj.getString("blurHash")
                                                else null,
                                        description =
                                                obj.optString("description", "Starter Collection"),
                                        photographerName =
                                                obj.optString("photographerName", "VIREX"),
                                        photographerUrl =
                                                if (obj.has("photographerUrl"))
                                                        obj.getString("photographerUrl")
                                                else null,
                                        sourceUrl = obj.getString("sourceUrl"),
                                        category = category,
                                        searchQuery = "starter",
                                        tags =
                                                listOf(
                                                        "starter",
                                                        "collection",
                                                        category.displayName.lowercase()
                                                ),
                                        likes = 0,
                                        syncedAt = System.currentTimeMillis(),
                                        viewed = false
                                )

                        wallpapers.add(wallpaper)
                    }

                    if (wallpapers.isNotEmpty()) {
                        syncedWallpaperDao.insertWallpapers(wallpapers)
                        Log.d(TAG, "✅ Loaded ${wallpapers.size} starter wallpapers")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load preload data", e)
                }
            }
}
