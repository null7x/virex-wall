package com.virex.wallpapers.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Picsum Photos API response model
 *
 * Last-resort fallback source.
 */
data class PicsumPhoto(
        @SerializedName("id") val id: String,
        @SerializedName("author") val author: String = "",
        @SerializedName("width") val width: Int = 0,
        @SerializedName("height") val height: Int = 0,
        @SerializedName("url") val url: String = "",
        @SerializedName("download_url") val downloadUrl: String = ""
) {
    /**
     * Get resized image URL via Picsum resize service Format:
     * https://picsum.photos/id/{id}/{width}/{height}
     */
    fun getResizedUrl(width: Int, height: Int): String {
        return "https://picsum.photos/id/$id/$width/$height"
    }

    /** Get thumbnail URL (400x700 portrait) */
    fun getThumbnailUrl(): String = getResizedUrl(400, 700)

    /** Get full resolution URL (1080x1920 portrait) */
    fun getFullUrl(): String = getResizedUrl(1080, 1920)
}
