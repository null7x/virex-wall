package com.virex.wallpapers.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.virex.wallpapers.data.model.CategoryPreference
import com.virex.wallpapers.data.model.InteractionType
import com.virex.wallpapers.data.model.UserInteraction
import com.virex.wallpapers.data.model.WeeklyStats
import kotlinx.coroutines.flow.Flow

/**
 * DAO for recommendation system
 * 
 * Handles user interaction tracking and recommendation queries.
 */
@Dao
interface RecommendationDao {
    
    // ==================== USER INTERACTIONS ====================
    
    /**
     * Log a user interaction
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun logInteraction(interaction: UserInteraction)
    
    /**
     * Get all interactions for a wallpaper
     */
    @Query("SELECT * FROM user_interactions WHERE wallpaperId = :wallpaperId ORDER BY timestamp DESC")
    fun getInteractionsForWallpaper(wallpaperId: String): Flow<List<UserInteraction>>
    
    /**
     * Get recent interactions
     */
    @Query("SELECT * FROM user_interactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentInteractions(limit: Int = 100): Flow<List<UserInteraction>>
    
    /**
     * Get interactions by type
     */
    @Query("SELECT * FROM user_interactions WHERE interactionType = :type ORDER BY timestamp DESC LIMIT :limit")
    fun getInteractionsByType(type: InteractionType, limit: Int = 50): Flow<List<UserInteraction>>
    
    /**
     * Get most viewed wallpaper IDs
     */
    @Query("""
        SELECT wallpaperId, COUNT(*) as count 
        FROM user_interactions 
        WHERE interactionType IN ('VIEW', 'DETAIL_VIEW') 
        AND timestamp > :since
        GROUP BY wallpaperId 
        ORDER BY count DESC 
        LIMIT :limit
    """)
    suspend fun getMostViewedWallpaperIds(since: Long, limit: Int = 20): List<WallpaperInteractionCount>
    
    /**
     * Get most downloaded wallpaper IDs
     */
    @Query("""
        SELECT wallpaperId, COUNT(*) as count 
        FROM user_interactions 
        WHERE interactionType = 'DOWNLOAD' 
        AND timestamp > :since
        GROUP BY wallpaperId 
        ORDER BY count DESC 
        LIMIT :limit
    """)
    suspend fun getMostDownloadedWallpaperIds(since: Long, limit: Int = 20): List<WallpaperInteractionCount>
    
    /**
     * Get favorite categories (most interacted)
     */
    @Query("""
        SELECT categoryId, SUM(
            CASE 
                WHEN interactionType = 'VIEW' THEN 1
                WHEN interactionType = 'DETAIL_VIEW' THEN 2
                WHEN interactionType = 'DOWNLOAD' THEN 5
                WHEN interactionType = 'SET_WALLPAPER' THEN 8
                WHEN interactionType = 'FAVORITE' THEN 10
                WHEN interactionType = 'SHARE' THEN 4
                WHEN interactionType = 'UNFAVORITE' THEN -5
                ELSE 0
            END
        ) as score
        FROM user_interactions
        WHERE timestamp > :since
        GROUP BY categoryId
        ORDER BY score DESC
        LIMIT :limit
    """)
    suspend fun getPreferredCategories(since: Long, limit: Int = 5): List<CategoryScore>
    
    /**
     * Get tags from user's favorite and downloaded wallpapers
     */
    @Query("""
        SELECT tags FROM user_interactions 
        WHERE interactionType IN ('FAVORITE', 'DOWNLOAD', 'SET_WALLPAPER')
        AND tags != ''
        ORDER BY timestamp DESC
        LIMIT :limit
    """)
    suspend fun getPreferredTags(limit: Int = 50): List<String>
    
    /**
     * Count interactions for a category
     */
    @Query("""
        SELECT COUNT(*) FROM user_interactions 
        WHERE categoryId = :categoryId 
        AND timestamp > :since
    """)
    suspend fun getCategoryInteractionCount(categoryId: String, since: Long): Int
    
    /**
     * Check if user has interacted with a wallpaper
     */
    @Query("SELECT EXISTS(SELECT 1 FROM user_interactions WHERE wallpaperId = :wallpaperId)")
    suspend fun hasInteractedWith(wallpaperId: String): Boolean
    
    /**
     * Get user's interaction score for a wallpaper
     */
    @Query("""
        SELECT SUM(
            CASE 
                WHEN interactionType = 'VIEW' THEN 1
                WHEN interactionType = 'DETAIL_VIEW' THEN 2
                WHEN interactionType = 'DOWNLOAD' THEN 5
                WHEN interactionType = 'SET_WALLPAPER' THEN 8
                WHEN interactionType = 'FAVORITE' THEN 10
                WHEN interactionType = 'SHARE' THEN 4
                WHEN interactionType = 'UNFAVORITE' THEN -5
                ELSE 0
            END
        ) FROM user_interactions WHERE wallpaperId = :wallpaperId
    """)
    suspend fun getWallpaperInteractionScore(wallpaperId: String): Int?
    
    /**
     * Delete old interactions (cleanup)
     */
    @Query("DELETE FROM user_interactions WHERE timestamp < :before")
    suspend fun deleteOldInteractions(before: Long): Int
    
    // ==================== CATEGORY PREFERENCES ====================
    
    /**
     * Update category preference
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateCategoryPreference(preference: CategoryPreference)
    
    /**
     * Get all category preferences
     */
    @Query("SELECT * FROM category_preferences ORDER BY score DESC")
    fun getCategoryPreferences(): Flow<List<CategoryPreference>>
    
    /**
     * Get top preferred categories
     */
    @Query("SELECT * FROM category_preferences ORDER BY score DESC LIMIT :limit")
    suspend fun getTopCategories(limit: Int = 3): List<CategoryPreference>
    
    // ==================== WEEKLY STATS ====================
    
    /**
     * Update weekly stats
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateWeeklyStats(stats: WeeklyStats)
    
    /**
     * Get popular this week
     */
    @Query("SELECT * FROM weekly_stats WHERE weekNumber = :weekNumber ORDER BY popularityScore DESC LIMIT :limit")
    suspend fun getPopularThisWeek(weekNumber: Int, limit: Int = 10): List<WeeklyStats>
    
    /**
     * Delete old weekly stats
     */
    @Query("DELETE FROM weekly_stats WHERE weekNumber < :beforeWeek")
    suspend fun deleteOldWeeklyStats(beforeWeek: Int): Int
    
    // ==================== SIMILAR WALLPAPERS QUERY ====================
    
    /**
     * Get wallpapers in same categories as user's favorites
     */
    @Query("""
        SELECT DISTINCT categoryId FROM user_interactions 
        WHERE interactionType = 'FAVORITE'
        ORDER BY timestamp DESC
        LIMIT :limit
    """)
    suspend fun getFavoriteCategoryIds(limit: Int = 5): List<String>
}

/**
 * Helper class for wallpaper interaction count
 */
data class WallpaperInteractionCount(
    val wallpaperId: String,
    val count: Int
)

/**
 * Helper class for category score
 */
data class CategoryScore(
    val categoryId: String,
    val score: Int
)
