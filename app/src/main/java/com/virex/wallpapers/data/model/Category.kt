package com.virex.wallpapers.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Category data model
 *
 * Represents a wallpaper category from Firebase Firestore.
 */
data class Category(
        @DocumentId val id: String = "",
        val name: String = "",
        val description: String = "",
        @get:PropertyName("icon_url") @set:PropertyName("icon_url") var iconUrl: String = "",
        @get:PropertyName("cover_url") @set:PropertyName("cover_url") var coverUrl: String = "",
        @get:PropertyName("wallpaper_count")
        @set:PropertyName("wallpaper_count")
        var wallpaperCount: Int = 0,
        @get:PropertyName("is_premium")
        @set:PropertyName("is_premium")
        var isPremium: Boolean = false,
        @get:PropertyName("sort_order") @set:PropertyName("sort_order") var sortOrder: Int = 0,
        @get:PropertyName("is_visible")
        @set:PropertyName("is_visible")
        var isVisible: Boolean = true
)

/** Predefined category icons for UI display */
object CategoryIcons {
    const val ABSTRACT = "abstract"
    const val NATURE = "nature"
    const val SPACE = "space"
    const val MINIMAL = "minimal"
    const val GEOMETRIC = "geometric"
    const val GRADIENT = "gradient"
    const val DARK = "dark"
    const val NEON = "neon"
    const val TEXTURE = "texture"
    const val PATTERN = "pattern"
}
