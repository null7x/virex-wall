package com.virex.wallpapers.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Synced Wallpaper Entity
 *
 * Represents wallpapers fetched from external APIs (Unsplash, Pexels). Stored locally for offline
 * access and deduplication.
 */
@Entity(
        tableName = "synced_wallpapers",
        indices =
                [
                        Index(value = ["sourceId", "source"], unique = true),
                        Index(value = ["category"]),
                        Index(value = ["syncedAt"])]
)
data class SyncedWallpaper(
        @PrimaryKey val id: String,

        /** Original ID from the source API */
        val sourceId: String,

        /** Source of the wallpaper: "unsplash" or "pexels" */
        val source: WallpaperSource,

        /** Thumbnail URL for grid display */
        val thumbnailUrl: String,

        /** Full resolution URL for download/setting */
        val fullUrl: String,

        /** Preview URL for detail screen */
        val previewUrl: String,

        /** Original image URL (highest quality) */
        val originalUrl: String,

        /** Image width in pixels */
        val width: Int,

        /** Image height in pixels */
        val height: Int,

        /** Average/dominant color (hex format) */
        val color: String? = null,

        /** BlurHash for placeholder */
        val blurHash: String? = null,

        /** Description or alt text */
        val description: String? = null,

        /** Photographer/artist name (required for attribution) */
        val photographerName: String,

        /** Photographer profile URL */
        val photographerUrl: String? = null,

        /** Source page URL (for attribution link) */
        val sourceUrl: String,

        /** Category: AMOLED, DARK, MINIMAL, NEW */
        val category: SyncCategory,

        /** Search query that found this wallpaper */
        val searchQuery: String? = null,

        /** Tags for searching */
        val tags: List<String> = emptyList(),

        /** Number of likes on source platform */
        val likes: Int = 0,

        /** When this wallpaper was synced */
        val syncedAt: Long = System.currentTimeMillis(),

        /** When the wallpaper was created on source */
        val createdAt: Long? = null,

        /** Whether the wallpaper has been viewed by user */
        val viewed: Boolean = false,

        /** Local cache file path (if cached) */
        val localCachePath: String? = null,

        /** Whether the wallpaper is cached locally */
        val isCached: Boolean = false
) {
    /** Get attribution text as required by API terms */
    fun getAttribution(): String {
        return when (source) {
            WallpaperSource.UNSPLASH -> "Photo by $photographerName on Unsplash"
            WallpaperSource.PEXELS -> "Photo by $photographerName on Pexels"
            WallpaperSource.WALLHAVEN -> "From Wallhaven"
            WallpaperSource.PICSUM -> "Photo by $photographerName via Picsum"
            WallpaperSource.FIREBASE -> photographerName
            WallpaperSource.GITHUB_CDN -> "VIREX Starter Collection"
        }
    }

    /** Check if this wallpaper is "new" (synced within last 7 days) */
    fun isNew(): Boolean {
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        return syncedAt > sevenDaysAgo
    }

    /** Get best URL for current use case */
    fun getBestUrlForSize(targetWidth: Int): String {
        return when {
            targetWidth <= 200 -> thumbnailUrl
            targetWidth <= 600 -> previewUrl
            targetWidth <= 1200 -> fullUrl
            else -> originalUrl
        }
    }

    /** Convert to Wallpaper model for use in Home screen */
    fun toWallpaper(): Wallpaper {
        return Wallpaper(
                id = id,
                title = description ?: photographerName,
                description = getAttribution(),
                thumbnailUrl = thumbnailUrl,
                fullUrl = fullUrl,
                categoryId = category.name.lowercase(),
                categoryName = category.displayName,
                width = width,
                height = height,
                fileSize = 0L,
                downloads = 0,
                likes = likes,
                isPremium = false,
                isFeatured = category == SyncCategory.AMOLED,
                isTrending = likes > 100,
                tags = tags,
                createdAt = syncedAt,
                updatedAt = syncedAt
        )
    }
}

/** Source of the synced wallpaper */
enum class WallpaperSource {
    UNSPLASH,
    PEXELS,
    WALLHAVEN,
    PICSUM,
    FIREBASE,
    GITHUB_CDN // Starter collection from bundled assets
}

/** Categories for synced wallpapers - MEGA collection */
enum class SyncCategory(val displayName: String, val searchTerms: List<String>) {
    AMOLED(
            displayName = "AMOLED",
            searchTerms =
                    listOf(
                            "amoled wallpaper",
                            "pure black",
                            "black background",
                            "dark abstract black"
                    )
    ),
    DARK(
            displayName = "Dark",
            searchTerms =
                    listOf("dark wallpaper", "dark aesthetic", "dark background", "moody dark")
    ),
    MINIMAL(
            displayName = "Minimal",
            searchTerms =
                    listOf(
                            "minimal wallpaper",
                            "minimalist dark",
                            "simple abstract",
                            "clean minimal"
                    )
    ),
    NATURE(
            displayName = "Nature",
            searchTerms =
                    listOf("nature wallpaper", "forest dark", "mountains night", "nature landscape")
    ),
    SPACE(
            displayName = "Space",
            searchTerms = listOf("space wallpaper", "galaxy", "cosmos", "nebula", "stars night sky")
    ),
    ABSTRACT(
            displayName = "Abstract",
            searchTerms =
                    listOf("abstract wallpaper", "abstract art", "geometric abstract", "fluid art")
    ),
    ANIME(
            displayName = "Anime",
            searchTerms = listOf("anime wallpaper", "anime dark", "manga art", "anime aesthetic")
    ),
    CYBERPUNK(
            displayName = "Cyberpunk",
            searchTerms =
                    listOf(
                            "cyberpunk wallpaper",
                            "neon city",
                            "cyberpunk aesthetic",
                            "futuristic city"
                    )
    ),
    CARS(
            displayName = "Cars",
            searchTerms = listOf("car wallpaper", "supercar", "sports car dark", "luxury car night")
    ),
    CITY(
            displayName = "City",
            searchTerms = listOf("city wallpaper", "city night", "urban skyline", "cityscape dark")
    ),
    GRADIENT(
            displayName = "Gradient",
            searchTerms =
                    listOf(
                            "gradient wallpaper",
                            "color gradient",
                            "gradient abstract",
                            "smooth gradient"
                    )
    ),
    NEON(
            displayName = "Neon",
            searchTerms = listOf("neon wallpaper", "neon lights", "neon glow", "neon aesthetic")
    ),
    FANTASY(
            displayName = "Fantasy",
            searchTerms = listOf("fantasy wallpaper", "fantasy art", "magical", "dragon fantasy")
    ),
    GAMING(
            displayName = "Gaming",
            searchTerms = listOf("gaming wallpaper", "game art", "video game", "esports")
    ),
    OCEAN(
            displayName = "Ocean",
            searchTerms = listOf("ocean wallpaper", "sea dark", "underwater", "ocean waves")
    ),
    MOUNTAIN(
            displayName = "Mountains",
            searchTerms = listOf("mountain wallpaper", "mountain night", "snowy peaks", "alps dark")
    ),
    FLOWERS(
            displayName = "Flowers",
            searchTerms =
                    listOf("flower wallpaper", "dark flowers", "rose dark", "floral aesthetic")
    ),
    SKULL(
            displayName = "Skull",
            searchTerms = listOf("skull wallpaper", "skull art", "skeleton dark", "skull aesthetic")
    ),
    TEXTURE(
            displayName = "Texture",
            searchTerms =
                    listOf(
                            "texture wallpaper",
                            "dark texture",
                            "material texture",
                            "surface pattern"
                    )
    ),
    NEW(
            displayName = "New",
            searchTerms =
                    listOf(
                            "trending wallpaper dark",
                            "popular dark aesthetic",
                            "modern abstract dark"
                    )
    )
}

/** Sync status for tracking sync operations */
@Entity(tableName = "sync_status")
data class SyncStatus(
        @PrimaryKey val id: String = "default",

        /** Last successful sync timestamp */
        val lastSyncAt: Long = 0L,

        /** Last sync source */
        val lastSyncSource: String? = null,

        /** Number of wallpapers synced in last operation */
        val lastSyncCount: Int = 0,

        /** Whether sync is currently in progress */
        val isSyncing: Boolean = false,

        /** Last error message if sync failed */
        val lastError: String? = null,

        /** Total wallpapers synced */
        val totalSynced: Int = 0,

        /** Current page for Unsplash */
        val unsplashPage: Int = 1,

        /** Current page for Pexels */
        val pexelsPage: Int = 1
)
