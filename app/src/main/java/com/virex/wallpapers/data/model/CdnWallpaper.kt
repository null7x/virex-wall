package com.virex.wallpapers.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * CDN Wallpaper Entity
 *
 * Stores wallpapers fetched from CDN sources:
 * - GitHub RAW JSON (primary)
 * - Wallhaven (backup)
 * - Picsum (last fallback)
 *
 * Serves as local cache for offline mode and instant startup.
 */
@Entity(
        tableName = "cdn_wallpapers",
        indices =
                [
                        Index(value = ["remoteId", "source"], unique = true),
                        Index(value = ["source"]),
                        Index(value = ["isPro"]),
                        Index(value = ["cachedAt"])]
)
data class CdnWallpaper(
        @PrimaryKey val id: String,

        /** Original ID from the remote source */
        val remoteId: String,

        /** Full resolution image URL */
        val imageUrl: String,

        /** Thumbnail URL for grid display */
        val thumbnailUrl: String,

        /** Whether this wallpaper requires PRO */
        val isPro: Boolean = false,

        /** Source identifier: "cdn", "wallhaven", "picsum" */
        val source: String,
        
        /** Category name (abstract, anime, nature, etc.) */
        val category: String = "",

        /** Image width in pixels */
        val width: Int = 0,

        /** Image height in pixels */
        val height: Int = 0,

        /** Author / photographer name */
        val author: String = "",

        /** Tags (comma-separated) */
        val tags: String = "",

        /** When this wallpaper was cached locally */
        val cachedAt: Long = System.currentTimeMillis()
)
