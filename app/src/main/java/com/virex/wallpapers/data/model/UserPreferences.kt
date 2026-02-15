package com.virex.wallpapers.data.model

/**
 * User preferences data model
 *
 * Stored in DataStore for persistence.
 */
data class UserPreferences(
        val isPro: Boolean = false,
        val autoSetWallpaper: Boolean = false,
        val downloadOnlyOnWifi: Boolean = true,
        val showPremiumBadge: Boolean = true,
        val cacheSize: Long = 0L,
        val lastSyncTime: Long = 0L
)

/** Billing state for in-app purchases */
sealed class BillingState {
    data object Loading : BillingState()
    data object NotPurchased : BillingState()
    data object Purchased : BillingState()
    data class Error(val message: String) : BillingState()
}

/** Wallpaper set target */
enum class WallpaperTarget {
    HOME_SCREEN,
    LOCK_SCREEN,
    BOTH
}

/** UI state wrapper for async operations */
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
    data object Empty : UiState<Nothing>()
}

/** Filter options for wallpaper list */
data class WallpaperFilter(
        val categoryId: String? = null,
        val isPremium: Boolean? = null,
        val isFeatured: Boolean? = null,
        val isTrending: Boolean? = null,
        val sortBy: SortOption = SortOption.NEWEST
)

/** Sort options for wallpaper list */
enum class SortOption {
    NEWEST,
    OLDEST,
    MOST_DOWNLOADED,
    MOST_LIKED
}
