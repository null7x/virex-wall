package com.virex.wallpapers.data.remote.model

import com.google.gson.annotations.SerializedName

data class BingImageArchiveResponse(
        @SerializedName("images") val images: List<BingImageItem> = emptyList()
)

data class BingImageItem(
        @SerializedName("url") val url: String,
        @SerializedName("copyright") val copyright: String? = null,
        @SerializedName("title") val title: String? = null,
        @SerializedName("startdate") val startDate: String? = null
)
