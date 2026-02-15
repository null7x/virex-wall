package com.virex.wallpapers.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/** Navigation destinations */
sealed class Screen(
        val route: String,
        val title: String,
        val selectedIcon: ImageVector? = null,
        val unselectedIcon: ImageVector? = null
) {
    // Main navigation destinations (bottom bar)
    data object Home :
            Screen(
                    route = "home",
                    title = "Home",
                    selectedIcon = Icons.Filled.Home,
                    unselectedIcon = Icons.Outlined.Home
            )

    data object Categories :
            Screen(
                    route = "categories",
                    title = "Categories",
                    selectedIcon = Icons.Outlined.Category,
                    unselectedIcon = Icons.Outlined.Category
            )

    data object Favorites :
            Screen(
                    route = "favorites",
                    title = "Favorites",
                    selectedIcon = Icons.Filled.Favorite,
                    unselectedIcon = Icons.Outlined.FavoriteBorder
            )

    data object Settings :
            Screen(
                    route = "settings",
                    title = "Settings",
                    selectedIcon = Icons.Filled.Settings,
                    unselectedIcon = Icons.Outlined.Settings
            )

    data object Generator :
            Screen(
                    route = "generator",
                    title = "AI Generator",
                    selectedIcon = Icons.Filled.AutoAwesome,
                    unselectedIcon = Icons.Outlined.AutoAwesome
            )

    data object AutoSync :
            Screen(
                    route = "auto_sync",
                    title = "Auto Sync",
                    selectedIcon = Icons.Filled.CloudSync,
                    unselectedIcon = Icons.Outlined.CloudSync
            )

    // Detail screens
    data object WallpaperPreview : Screen(route = "wallpaper/{wallpaperId}", title = "Preview") {
        fun createRoute(wallpaperId: String) = "wallpaper/$wallpaperId"
    }

    data object CategoryDetail : Screen(route = "category/{categoryId}", title = "Category") {
        fun createRoute(categoryId: String) = "category/$categoryId"
    }

    data object SyncedWallpaperDetail :
            Screen(route = "synced_wallpaper/{syncedWallpaperId}", title = "Synced Wallpaper") {
        fun createRoute(wallpaperId: String) = "synced_wallpaper/$wallpaperId"
    }

    data object ProUpgrade : Screen(route = "pro", title = "VIREX PRO")

    data object PrivacyPolicy : Screen(route = "privacy_policy", title = "Privacy Policy")

    data object TermsOfService : Screen(route = "terms_of_service", title = "Terms of Service")

    companion object {
        // 5-item bottom nav: Explore, Collections, AI Generator (center FAB), Auto Sync, Favorites
        val bottomNavItems = listOf(Home, Categories, Generator, AutoSync, Favorites)
    }
}

/** Navigation arguments */
object NavArgs {
    const val WALLPAPER_ID = "wallpaperId"
    const val CATEGORY_ID = "categoryId"
    const val SYNCED_WALLPAPER_ID = "syncedWallpaperId"
}
