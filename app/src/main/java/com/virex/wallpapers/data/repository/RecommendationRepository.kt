package com.virex.wallpapers.data.repository

import android.util.Log
import com.virex.wallpapers.data.local.RecommendationDao
import com.virex.wallpapers.data.local.SyncedWallpaperDao
import com.virex.wallpapers.data.local.WallpaperDao
import com.virex.wallpapers.data.model.CategoryPreference
import com.virex.wallpapers.data.model.InteractionType
import com.virex.wallpapers.data.model.RecommendationReason
import com.virex.wallpapers.data.model.RecommendedWallpaper
import com.virex.wallpapers.data.model.SyncCategory
import com.virex.wallpapers.data.model.UserInteraction
import com.virex.wallpapers.data.model.Wallpaper
import com.virex.wallpapers.data.model.WeeklyStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Recommendation Repository
 * 
 * Implements a privacy-friendly, local recommendation system.
 * 
 * ALGORITHM OVERVIEW:
 * 
 * 1. **Category Affinity**: Tracks which categories user prefers based on
 *    views, downloads, favorites. Each interaction type has a weight.
 * 
 * 2. **Tag Matching**: Analyzes tags from favorited/downloaded wallpapers
 *    to find similar content.
 * 
 * 3. **Recency Decay**: Recent interactions count more than old ones.
 *    Uses exponential decay with 7-day half-life.
 * 
 * 4. **Diversity Boost**: Prevents filter bubble by including some
 *    wallpapers from less-explored categories.
 * 
 * 5. **Popularity Signal**: Combines community popularity (downloads/likes)
 *    with personal preferences.
 * 
 * SCORING FORMULA:
 * ```
 * score = (categoryAffinity * 0.35) + 
 *         (tagSimilarity * 0.25) + 
 *         (popularity * 0.20) + 
 *         (freshness * 0.15) +
 *         (diversityBonus * 0.05)
 * ```
 * 
 * All data is stored locally in Room database.
 * Nothing is sent to servers - fully privacy-friendly.
 */
@Singleton
class RecommendationRepository @Inject constructor(
    private val recommendationDao: RecommendationDao,
    private val wallpaperDao: WallpaperDao,
    private val syncedWallpaperDao: SyncedWallpaperDao
) {
    
    companion object {
        private const val TAG = "RecommendationRepo"
        
        // Time constants
        private const val ONE_WEEK_MS = 7 * 24 * 60 * 60 * 1000L
        private const val ONE_MONTH_MS = 30 * 24 * 60 * 60 * 1000L
        private const val RECENCY_HALF_LIFE_MS = ONE_WEEK_MS
        
        // Score weights
        private const val WEIGHT_CATEGORY = 0.35f
        private const val WEIGHT_TAGS = 0.25f
        private const val WEIGHT_POPULARITY = 0.20f
        private const val WEIGHT_FRESHNESS = 0.15f
        private const val WEIGHT_DIVERSITY = 0.05f
        
        // Limits
        private const val MAX_RECOMMENDATIONS = 20
        private const val MIN_INTERACTIONS_FOR_PERSONALIZATION = 5
    }
    
    // ==================== INTERACTION TRACKING ====================
    
    /**
     * Log a user interaction with a wallpaper
     */
    suspend fun logInteraction(
        wallpaperId: String,
        categoryId: String,
        type: InteractionType,
        durationMs: Long = 0,
        tags: List<String> = emptyList()
    ) {
        withContext(Dispatchers.IO) {
            try {
                val interaction = UserInteraction(
                    wallpaperId = wallpaperId,
                    categoryId = categoryId,
                    interactionType = type,
                    durationMs = durationMs,
                    tags = tags.joinToString(",")
                )
                recommendationDao.logInteraction(interaction)
                
                // Update category preference
                updateCategoryPreference(categoryId, type)
                
                // Update weekly stats
                updateWeeklyStats(wallpaperId, type)
                
                Log.d(TAG, "Logged interaction: $type for $wallpaperId")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to log interaction", e)
            }
        }
    }
    
    /**
     * Update category preference based on interaction
     */
    private suspend fun updateCategoryPreference(categoryId: String, type: InteractionType) {
        val topCategories = recommendationDao.getTopCategories(10)
        val existing = topCategories.find { it.categoryId == categoryId }
        
        val newPreference = CategoryPreference(
            categoryId = categoryId,
            score = (existing?.score ?: 0f) + type.weight,
            interactionCount = (existing?.interactionCount ?: 0) + 1,
            lastInteractionAt = System.currentTimeMillis()
        )
        recommendationDao.updateCategoryPreference(newPreference)
    }
    
    /**
     * Update weekly stats for "Popular this week"
     */
    private suspend fun updateWeeklyStats(wallpaperId: String, type: InteractionType) {
        val weekNumber = getCurrentWeekNumber()
        val existing = recommendationDao.getPopularThisWeek(weekNumber, 100)
            .find { it.wallpaperId == wallpaperId }
        
        val viewDelta = if (type == InteractionType.VIEW || type == InteractionType.DETAIL_VIEW) 1 else 0
        val downloadDelta = if (type == InteractionType.DOWNLOAD) 1 else 0
        val favoriteDelta = if (type == InteractionType.FAVORITE) 1 else 0
        
        val newStats = WeeklyStats(
            wallpaperId = wallpaperId,
            weekNumber = weekNumber,
            viewCount = (existing?.viewCount ?: 0) + viewDelta,
            downloadCount = (existing?.downloadCount ?: 0) + downloadDelta,
            favoriteCount = (existing?.favoriteCount ?: 0) + favoriteDelta,
            popularityScore = calculatePopularityScore(
                (existing?.viewCount ?: 0) + viewDelta,
                (existing?.downloadCount ?: 0) + downloadDelta,
                (existing?.favoriteCount ?: 0) + favoriteDelta
            )
        )
        recommendationDao.updateWeeklyStats(newStats)
    }
    
    /**
     * Get current week number (year * 100 + week)
     */
    private fun getCurrentWeekNumber(): Int {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val week = calendar.get(Calendar.WEEK_OF_YEAR)
        return year * 100 + week
    }
    
    /**
     * Calculate popularity score from interaction counts
     */
    private fun calculatePopularityScore(views: Int, downloads: Int, favorites: Int): Float {
        return views * 1f + downloads * 5f + favorites * 10f
    }
    
    // ==================== RECOMMENDATIONS ====================
    
    /**
     * Get "Recommended for you" wallpapers
     * 
     * Personalized recommendations based on user's viewing history,
     * downloads, and favorites.
     */
    fun getRecommendedForYou(): Flow<List<RecommendedWallpaper>> = flow {
        Log.d(TAG, "Getting personalized recommendations...")
        
        val since = System.currentTimeMillis() - ONE_MONTH_MS
        val preferredCategories = recommendationDao.getPreferredCategories(since, 5)
        val preferredTags = recommendationDao.getPreferredTags(50)
        val parsedTags = preferredTags.flatMap { it.split(",") }.filter { it.isNotBlank() }
        
        // Check if we have enough data for personalization
        val hasEnoughData = preferredCategories.isNotEmpty()
        
        if (!hasEnoughData) {
            // Fallback to popular/trending
            Log.d(TAG, "Not enough data for personalization, using popular")
            emit(getPopularFallback())
            return@flow
        }
        
        // Get all candidate wallpapers
        val allWallpapers = getAllAvailableWallpapers()
        
        // Score each wallpaper
        val scored = allWallpapers.map { wallpaper ->
            val score = calculateRecommendationScore(
                wallpaper = wallpaper,
                preferredCategories = preferredCategories,
                preferredTags = parsedTags
            )
            RecommendedWallpaper(
                wallpaper = wallpaper,
                score = score.first,
                reason = score.second,
                reasonText = getReasonText(score.second)
            )
        }
        
        // Sort by score and take top results
        val recommendations = scored
            .filter { it.score > 0 }
            .sortedByDescending { it.score }
            .take(MAX_RECOMMENDATIONS)
        
        Log.d(TAG, "Generated ${recommendations.size} personalized recommendations")
        emit(recommendations)
    }
    
    /**
     * Get "Similar wallpapers" based on a given wallpaper
     */
    suspend fun getSimilarWallpapers(
        wallpaperId: String,
        categoryId: String,
        tags: List<String>,
        limit: Int = 10
    ): List<RecommendedWallpaper> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Getting similar wallpapers for $wallpaperId")
        
        val allWallpapers = getAllAvailableWallpapers()
            .filter { it.id != wallpaperId } // Exclude the source wallpaper
        
        val scored = allWallpapers.map { wallpaper ->
            val similarity = calculateSimilarity(
                targetCategoryId = categoryId,
                targetTags = tags,
                candidateCategoryId = wallpaper.categoryId,
                candidateTags = wallpaper.tags
            )
            RecommendedWallpaper(
                wallpaper = wallpaper,
                score = similarity,
                reason = RecommendationReason.SIMILAR_TO_FAVORITES,
                reasonText = "Similar style"
            )
        }
        
        scored
            .filter { it.score > 0.1f }
            .sortedByDescending { it.score }
            .take(limit)
    }
    
    /**
     * Get "Popular this week" wallpapers
     */
    fun getPopularThisWeek(): Flow<List<RecommendedWallpaper>> = flow {
        Log.d(TAG, "Getting popular this week...")
        
        val weekNumber = getCurrentWeekNumber()
        val weeklyStats = recommendationDao.getPopularThisWeek(weekNumber, MAX_RECOMMENDATIONS)
        
        if (weeklyStats.isEmpty()) {
            // Fallback to all-time popular
            emit(getPopularFallback())
            return@flow
        }
        
        val wallpaperIds = weeklyStats.map { it.wallpaperId }
        val allWallpapers = getAllAvailableWallpapers()
        val wallpaperMap = allWallpapers.associateBy { it.id }
        
        val recommendations = weeklyStats.mapNotNull { stats ->
            wallpaperMap[stats.wallpaperId]?.let { wallpaper ->
                RecommendedWallpaper(
                    wallpaper = wallpaper,
                    score = stats.popularityScore,
                    reason = RecommendationReason.POPULAR_THIS_WEEK,
                    reasonText = "🔥 Popular this week"
                )
            }
        }
        
        Log.d(TAG, "Found ${recommendations.size} popular this week")
        emit(recommendations)
    }
    
    /**
     * Get trending wallpapers (combination of likes and recent activity)
     */
    fun getTrending(): Flow<List<RecommendedWallpaper>> = flow {
        Log.d(TAG, "Getting trending...")
        
        val allWallpapers = getAllAvailableWallpapers()
        
        val scored = allWallpapers.map { wallpaper ->
            val trendScore = calculateTrendScore(wallpaper)
            RecommendedWallpaper(
                wallpaper = wallpaper,
                score = trendScore,
                reason = RecommendationReason.TRENDING_NOW,
                reasonText = "📈 Trending"
            )
        }
        
        val trending = scored
            .sortedByDescending { it.score }
            .take(MAX_RECOMMENDATIONS)
        
        emit(trending)
    }
    
    // ==================== SCORING ALGORITHMS ====================
    
    /**
     * Calculate recommendation score for a wallpaper
     * 
     * Returns (score, primary reason)
     */
    private suspend fun calculateRecommendationScore(
        wallpaper: Wallpaper,
        preferredCategories: List<com.virex.wallpapers.data.local.CategoryScore>,
        preferredTags: List<String>
    ): Pair<Float, RecommendationReason> {
        var score = 0f
        var primaryReason = RecommendationReason.BASED_ON_HISTORY
        var maxComponentScore = 0f
        
        // 1. Category affinity
        val categoryScore = preferredCategories
            .find { it.categoryId == wallpaper.categoryId }
            ?.score?.toFloat()?.div(100f)?.coerceAtMost(1f) ?: 0f
        score += categoryScore * WEIGHT_CATEGORY
        if (categoryScore > maxComponentScore) {
            maxComponentScore = categoryScore
            primaryReason = RecommendationReason.POPULAR_IN_CATEGORY
        }
        
        // 2. Tag similarity
        val tagScore = calculateTagSimilarity(preferredTags, wallpaper.tags)
        score += tagScore * WEIGHT_TAGS
        if (tagScore > maxComponentScore) {
            maxComponentScore = tagScore
            primaryReason = RecommendationReason.SIMILAR_TAGS
        }
        
        // 3. Popularity
        val popularityScore = calculateNormalizedPopularity(wallpaper)
        score += popularityScore * WEIGHT_POPULARITY
        if (popularityScore > maxComponentScore) {
            maxComponentScore = popularityScore
            primaryReason = RecommendationReason.HIGHLY_RATED
        }
        
        // 4. Freshness
        val freshnessScore = calculateFreshness(wallpaper.createdAt)
        score += freshnessScore * WEIGHT_FRESHNESS
        if (freshnessScore > 0.8f && freshnessScore > maxComponentScore) {
            primaryReason = RecommendationReason.NEW_IN_PREFERRED_CATEGORY
        }
        
        // 5. Diversity bonus (boost for less-explored categories)
        val diversityScore = if (categoryScore < 0.1f) 0.5f else 0f
        score += diversityScore * WEIGHT_DIVERSITY
        
        // Penalty for already-interacted wallpapers
        if (recommendationDao.hasInteractedWith(wallpaper.id)) {
            score *= 0.3f // Reduce score but don't eliminate
        }
        
        return Pair(score, primaryReason)
    }
    
    /**
     * Calculate similarity between two wallpapers
     */
    private fun calculateSimilarity(
        targetCategoryId: String,
        targetTags: List<String>,
        candidateCategoryId: String,
        candidateTags: List<String>
    ): Float {
        var score = 0f
        
        // Same category = high similarity
        if (targetCategoryId == candidateCategoryId) {
            score += 0.5f
        }
        
        // Tag overlap
        val tagScore = calculateTagSimilarity(targetTags, candidateTags)
        score += tagScore * 0.5f
        
        return score
    }
    
    /**
     * Calculate tag similarity using Jaccard index
     */
    private fun calculateTagSimilarity(tags1: List<String>, tags2: List<String>): Float {
        if (tags1.isEmpty() || tags2.isEmpty()) return 0f
        
        val set1 = tags1.map { it.lowercase().trim() }.toSet()
        val set2 = tags2.map { it.lowercase().trim() }.toSet()
        
        val intersection = set1.intersect(set2).size
        val union = set1.union(set2).size
        
        return if (union > 0) intersection.toFloat() / union else 0f
    }
    
    /**
     * Calculate normalized popularity (0-1)
     */
    private fun calculateNormalizedPopularity(wallpaper: Wallpaper): Float {
        val maxLikes = 1000f
        val maxDownloads = 500f
        
        val likeScore = (wallpaper.likes.toFloat() / maxLikes).coerceAtMost(1f)
        val downloadScore = (wallpaper.downloads.toFloat() / maxDownloads).coerceAtMost(1f)
        
        return likeScore * 0.6f + downloadScore * 0.4f
    }
    
    /**
     * Calculate freshness score (higher for newer wallpapers)
     */
    private fun calculateFreshness(createdAt: Long): Float {
        val age = System.currentTimeMillis() - createdAt
        val oneWeek = ONE_WEEK_MS
        
        return when {
            age < oneWeek -> 1f
            age < oneWeek * 2 -> 0.8f
            age < oneWeek * 4 -> 0.5f
            else -> 0.2f
        }
    }
    
    /**
     * Calculate trend score
     */
    private fun calculateTrendScore(wallpaper: Wallpaper): Float {
        val popularity = calculateNormalizedPopularity(wallpaper)
        val freshness = calculateFreshness(wallpaper.createdAt)
        
        // Trending = popular + fresh
        return popularity * 0.6f + freshness * 0.4f
    }
    
    // ==================== HELPERS ====================
    
    /**
     * Get all available wallpapers (from synced + Firebase cache)
     */
    private suspend fun getAllAvailableWallpapers(): List<Wallpaper> {
        val cachedWallpapers = try {
            wallpaperDao.getAllWallpapers().first()
        } catch (e: Exception) {
            emptyList()
        }
        
        val syncedWallpapers = try {
            syncedWallpaperDao.getAllSyncedWallpapers().first()
                .map { it.toWallpaper() }
        } catch (e: Exception) {
            emptyList()
        }
        
        return (cachedWallpapers + syncedWallpapers).distinctBy { it.id }
    }
    
    /**
     * Fallback to popular wallpapers when not enough personalization data
     */
    private suspend fun getPopularFallback(): List<RecommendedWallpaper> {
        val allWallpapers = getAllAvailableWallpapers()
        
        return allWallpapers
            .sortedByDescending { it.likes + it.downloads * 2 }
            .take(MAX_RECOMMENDATIONS)
            .map { wallpaper ->
                RecommendedWallpaper(
                    wallpaper = wallpaper,
                    score = (wallpaper.likes + wallpaper.downloads * 2).toFloat(),
                    reason = RecommendationReason.HIGHLY_RATED,
                    reasonText = "⭐ Highly rated"
                )
            }
    }
    
    /**
     * Get human-readable reason text
     */
    private fun getReasonText(reason: RecommendationReason): String {
        return when (reason) {
            RecommendationReason.SIMILAR_TO_FAVORITES -> "Similar to your favorites"
            RecommendationReason.POPULAR_IN_CATEGORY -> "Popular in your favorite category"
            RecommendationReason.TRENDING_NOW -> "📈 Trending"
            RecommendationReason.POPULAR_THIS_WEEK -> "🔥 Popular this week"
            RecommendationReason.BASED_ON_HISTORY -> "Based on your history"
            RecommendationReason.NEW_IN_PREFERRED_CATEGORY -> "✨ New in your style"
            RecommendationReason.SIMILAR_TAGS -> "Similar style"
            RecommendationReason.HIGHLY_RATED -> "⭐ Highly rated"
        }
    }
    
    // ==================== CLEANUP ====================
    
    /**
     * Clean up old data to save storage
     */
    suspend fun cleanup() {
        withContext(Dispatchers.IO) {
            try {
                // Delete interactions older than 3 months
                val threeMonthsAgo = System.currentTimeMillis() - (90 * 24 * 60 * 60 * 1000L)
                val deletedInteractions = recommendationDao.deleteOldInteractions(threeMonthsAgo)
                
                // Delete weekly stats older than 4 weeks
                val fourWeeksAgo = getCurrentWeekNumber() - 4
                val deletedStats = recommendationDao.deleteOldWeeklyStats(fourWeeksAgo)
                
                Log.d(TAG, "Cleanup: deleted $deletedInteractions interactions, $deletedStats weekly stats")
            } catch (e: Exception) {
                Log.e(TAG, "Cleanup failed", e)
            }
        }
    }
}
