package com.virex.wallpapers.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

/**
 * Wallpaper data model
 *
 * Represents a wallpaper item from Firebase Firestore. Also used as Room entity for offline
 * caching.
 */
@Entity(tableName = "wallpapers")
data class Wallpaper(
        @PrimaryKey val id: String = "",
        val title: String = "",
        val description: String = "",
        @get:PropertyName("thumbnail_url")
        @set:PropertyName("thumbnail_url")
        var thumbnailUrl: String = "",
        @get:PropertyName("full_url") @set:PropertyName("full_url") var fullUrl: String = "",
        @get:PropertyName("category_id")
        @set:PropertyName("category_id")
        var categoryId: String = "",
        @get:PropertyName("category_name")
        @set:PropertyName("category_name")
        var categoryName: String = "",
        val width: Int = 0,
        val height: Int = 0,
        @get:PropertyName("file_size") @set:PropertyName("file_size") var fileSize: Long = 0L,
        val downloads: Int = 0,
        val likes: Int = 0,
        @get:PropertyName("is_premium")
        @set:PropertyName("is_premium")
        var isPremium: Boolean = false,
        @get:PropertyName("is_featured")
        @set:PropertyName("is_featured")
        var isFeatured: Boolean = false,
        @get:PropertyName("is_trending")
        @set:PropertyName("is_trending")
        var isTrending: Boolean = false,
        val tags: List<String> = emptyList(),
        @get:PropertyName("created_at")
        @set:PropertyName("created_at")
        var createdAt: Long = System.currentTimeMillis(),
        @get:PropertyName("updated_at")
        @set:PropertyName("updated_at")
        var updatedAt: Long = System.currentTimeMillis(),
        @get:PropertyName("is_new") @set:PropertyName("is_new") var isNewFlag: Boolean = false,
        val source: String = ""
) {
    /** Check if this is a new wallpaper (added within last 7 days) */
    fun isNew(): Boolean {
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        return createdAt > sevenDaysAgo
    }

    /** Get formatted file size string */
    fun getFormattedFileSize(): String {
        return when {
            fileSize < 1024 -> "$fileSize B"
            fileSize < 1024 * 1024 -> "${fileSize / 1024} KB"
            else -> "${fileSize / (1024 * 1024)} MB"
        }
    }

    /** Get formatted resolution string */
    fun getResolution(): String = "${width}x$height"

    /** Get aspect ratio */
    fun getAspectRatio(): Float = if (height > 0) width.toFloat() / height else 1f
}
