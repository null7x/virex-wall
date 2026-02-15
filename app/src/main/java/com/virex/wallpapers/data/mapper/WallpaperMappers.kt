package com.virex.wallpapers.data.mapper

import com.virex.wallpapers.data.model.CdnWallpaper
import com.virex.wallpapers.data.model.Wallpaper
import com.virex.wallpapers.data.remote.model.GitHubCdnWallpaper
import com.virex.wallpapers.data.remote.model.PicsumPhoto
import com.virex.wallpapers.data.remote.model.WallhavenWallpaper

/**
 * Mappers from API DTOs → CdnWallpaper (Room entity) → Wallpaper (domain model)
 *
 * All external sources are mapped into a single unified model.
 */
object WallpaperMappers {

    // ==================== Source Constants ====================

    const val SOURCE_CDN = "cdn"
    const val SOURCE_WALLHAVEN = "wallhaven"
    const val SOURCE_PICSUM = "picsum"

    // ==================== GitHub CDN → CdnWallpaper ====================

    fun GitHubCdnWallpaper.toCdnWallpaper(): CdnWallpaper {
        return CdnWallpaper(
                id = "cdn_$id",
                remoteId = id,
                imageUrl = image,
                thumbnailUrl = thumb,
                isPro = isPro,
                source = SOURCE_CDN,
                category = category,
                width = 0,
                height = 0,
                author = "",
                tags = "",
                cachedAt = System.currentTimeMillis()
        )
    }

    // ==================== Wallhaven → CdnWallpaper ====================

    fun WallhavenWallpaper.toCdnWallpaper(): CdnWallpaper {
        return CdnWallpaper(
                id = "wh_$id",
                remoteId = id,
                imageUrl = path,
                thumbnailUrl = thumbs?.large ?: thumbs?.original ?: path,
                isPro = false,
                source = SOURCE_WALLHAVEN,
                width = dimensionX,
                height = dimensionY,
                author = "",
                tags = tags?.joinToString(",") { it.name } ?: "",
                cachedAt = System.currentTimeMillis()
        )
    }

    // ==================== Picsum → CdnWallpaper ====================

    fun PicsumPhoto.toCdnWallpaper(): CdnWallpaper {
        return CdnWallpaper(
                id = "picsum_$id",
                remoteId = id,
                imageUrl = getFullUrl(),
                thumbnailUrl = getThumbnailUrl(),
                isPro = false,
                source = SOURCE_PICSUM,
                width = width,
                height = height,
                author = author,
                tags = "",
                cachedAt = System.currentTimeMillis()
        )
    }

    // ==================== CdnWallpaper → Wallpaper (domain) ====================

    fun CdnWallpaper.toWallpaper(): Wallpaper {
        return Wallpaper(
                id = id,
                title =
                        when (source) {
                            SOURCE_CDN -> "VIREX Wallpaper"
                            SOURCE_WALLHAVEN -> "Wallhaven #$remoteId"
                            SOURCE_PICSUM ->
                                    if (author.isNotBlank()) "Photo by $author"
                                    else "Picsum #$remoteId"
                            else -> "Wallpaper"
                        },
                description =
                        when (source) {
                            SOURCE_CDN -> "VIREX CDN"
                            SOURCE_WALLHAVEN -> "Wallhaven"
                            SOURCE_PICSUM -> author
                            else -> ""
                        },
                thumbnailUrl = thumbnailUrl,
                fullUrl = imageUrl,
                categoryId = category.ifBlank { "cdn" },
                categoryName = category.replaceFirstChar { it.uppercase() }.ifBlank { "CDN" },
                width = width,
                height = height,
                fileSize = 0L,
                downloads = 0,
                likes = 0,
                isPremium = isPro,
                isFeatured = false,
                isTrending = false,
                tags = if (tags.isNotBlank()) tags.split(",") else emptyList(),
                createdAt = cachedAt,
                updatedAt = cachedAt,
                source = source
        )
    }

    // ==================== Batch conversions ====================

    fun List<GitHubCdnWallpaper>.toCdnWallpapers(): List<CdnWallpaper> = map { it.toCdnWallpaper() }

    fun List<WallhavenWallpaper>.wallhavenToCdnWallpapers(): List<CdnWallpaper> = map {
        it.toCdnWallpaper()
    }

    fun List<PicsumPhoto>.picsumToCdnWallpapers(): List<CdnWallpaper> = map { it.toCdnWallpaper() }

    fun List<CdnWallpaper>.toWallpapers(): List<Wallpaper> = map { it.toWallpaper() }
}
