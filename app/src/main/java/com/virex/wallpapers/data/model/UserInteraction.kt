package com.virex.wallpapers.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * User Interaction Entity
 * 
 * Tracks all user interactions with wallpapers for the recommendation system.
 * This is stored locally and never leaves the device (privacy-friendly).
 */
@Entity(
    tableName = "user_interactions",
    indices = [
        Index(value = ["wallpaperId"]),
        Index(value = ["interactionType"]),
        Index(value = ["categoryId"]),
        Index(value = ["timestamp"])
    ]
)
data class UserInteraction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** Wallpaper ID this interaction is about */
    val wallpaperId: String,
    
    /** Category of the wallpaper */
    val categoryId: String,
    
    /** Type of interaction */
    val interactionType: InteractionType,
    
    /** When this interaction happened */
    val timestamp: Long = System.currentTimeMillis(),
    
    /** Duration in milliseconds (for VIEW type) */
    val durationMs: Long = 0,
    
    /** Tags of the wallpaper for better recommendations */
    val tags: String = ""
)

/**
 * Types of user interactions
 */
enum class InteractionType(val weight: Int) {
    /** User viewed the wallpaper preview */
    VIEW(1),
    
    /** User opened full detail screen */
    DETAIL_VIEW(2),
    
    /** User downloaded the wallpaper */
    DOWNLOAD(5),
    
    /** User set as wallpaper */
    SET_WALLPAPER(8),
    
    /** User added to favorites */
    FAVORITE(10),
    
    /** User shared the wallpaper */
    SHARE(4),
    
    /** User removed from favorites (negative weight) */
    UNFAVORITE(-5)
}

/**
 * Category preference score
 * 
 * Aggregated score for each category based on user interactions.
 */
@Entity(tableName = "category_preferences")
data class CategoryPreference(
    @PrimaryKey
    val categoryId: String,
    
    /** Aggregated score from all interactions */
    val score: Float = 0f,
    
    /** Number of interactions with this category */
    val interactionCount: Int = 0,
    
    /** Last interaction timestamp */
    val lastInteractionAt: Long = 0
)

/**
 * Wallpaper score for recommendations
 * 
 * Calculated score for each wallpaper based on various factors.
 */
data class WallpaperScore(
    val wallpaperId: String,
    val score: Float,
    val reason: RecommendationReason
)

/**
 * Reason why a wallpaper is recommended
 */
enum class RecommendationReason {
    SIMILAR_TO_FAVORITES,
    POPULAR_IN_CATEGORY,
    TRENDING_NOW,
    POPULAR_THIS_WEEK,
    BASED_ON_HISTORY,
    NEW_IN_PREFERRED_CATEGORY,
    SIMILAR_TAGS,
    HIGHLY_RATED
}

/**
 * Recommendation result with metadata
 */
data class RecommendedWallpaper(
    val wallpaper: Wallpaper,
    val score: Float,
    val reason: RecommendationReason,
    val reasonText: String
)

/**
 * Weekly statistics for "Popular this week"
 */
@Entity(tableName = "weekly_stats")
data class WeeklyStats(
    @PrimaryKey
    val wallpaperId: String,
    
    /** Week number (year * 100 + week) */
    val weekNumber: Int,
    
    /** Number of views this week */
    val viewCount: Int = 0,
    
    /** Number of downloads this week */
    val downloadCount: Int = 0,
    
    /** Number of favorites this week */
    val favoriteCount: Int = 0,
    
    /** Calculated popularity score */
    val popularityScore: Float = 0f
)
