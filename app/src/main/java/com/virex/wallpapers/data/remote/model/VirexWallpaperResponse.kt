package com.virex.wallpapers.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Response model for VIREX Backend wallpapers
 */
data class VirexWallpaperResponse(
    val wallpapers: List<VirexWallpaper>,
    val page: Int,
    @SerializedName("per_page")
    val perPage: Int,
    val query: String? = null,
    val category: String? = null
)

/**
 * Wallpaper model from VIREX Backend
 * 
 * Unified format for wallpapers from any source (Pexels, Unsplash, Wallhaven)
 */
data class VirexWallpaper(
    val id: String,
    val title: String,
    val url: String,
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String,
    val width: Int,
    val height: Int,
    val source: String, // "pexels", "unsplash", "wallhaven"
    val photographer: String? = null,
    @SerializedName("photographer_url")
    val photographerUrl: String? = null,
    val color: String? = null,
    val tags: List<String>? = null
)
