package com.virex.wallpapers.data.remote.model

import com.google.gson.annotations.SerializedName

/** Wallhaven API response models */
data class WallhavenSearchResponse(
        @SerializedName("data") val data: List<WallhavenWallpaper>,
        @SerializedName("meta") val meta: WallhavenMeta?
)

data class WallhavenWallpaper(
        @SerializedName("id") val id: String,
        @SerializedName("url") val url: String,
        @SerializedName("short_url") val shortUrl: String?,
        @SerializedName("views") val views: Int = 0,
        @SerializedName("favorites") val favorites: Int = 0,
        @SerializedName("source") val source: String = "",
        @SerializedName("purity") val purity: String = "",
        @SerializedName("category") val category: String = "",
        @SerializedName("dimension_x") val dimensionX: Int = 0,
        @SerializedName("dimension_y") val dimensionY: Int = 0,
        @SerializedName("resolution") val resolution: String = "",
        @SerializedName("ratio") val ratio: String = "",
        @SerializedName("file_size") val fileSize: Long = 0,
        @SerializedName("file_type") val fileType: String = "",
        @SerializedName("created_at") val createdAt: String = "",
        @SerializedName("colors") val colors: List<String> = emptyList(),
        @SerializedName("path") val path: String = "",
        @SerializedName("thumbs") val thumbs: WallhavenThumbs? = null,
        @SerializedName("tags") val tags: List<WallhavenTag>? = null
)

data class WallhavenThumbs(
        @SerializedName("large") val large: String = "",
        @SerializedName("original") val original: String = "",
        @SerializedName("small") val small: String = ""
)

data class WallhavenTag(
        @SerializedName("id") val id: Int,
        @SerializedName("name") val name: String,
        @SerializedName("alias") val alias: String? = null,
        @SerializedName("category_id") val categoryId: Int? = null,
        @SerializedName("category") val category: String? = null,
        @SerializedName("purity") val purity: String? = null
)

data class WallhavenMeta(
        @SerializedName("current_page") val currentPage: Int,
        @SerializedName("last_page") val lastPage: Int,
        @SerializedName("per_page") val perPage: Int,
        @SerializedName("total") val total: Int
)
