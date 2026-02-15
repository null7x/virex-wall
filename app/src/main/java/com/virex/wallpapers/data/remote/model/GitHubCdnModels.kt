package com.virex.wallpapers.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * GitHub CDN wallpaper JSON model
 *
 * JSON format: [ {
 * ```
 *     "id": "1",
 *     "image": "https://cdn-domain.com/wallpapers/1.jpg",
 *     "thumb": "https://cdn-domain.com/thumbs/1.jpg",
 *     "isPro": false
 * ```
 * } ]
 */
data class GitHubCdnWallpaper(
        @SerializedName("id") val id: String,
        @SerializedName("image") val image: String,
        @SerializedName("thumb") val thumb: String,
        @SerializedName("category") val category: String = "",
        @SerializedName("isPro") val isPro: Boolean = false
)
