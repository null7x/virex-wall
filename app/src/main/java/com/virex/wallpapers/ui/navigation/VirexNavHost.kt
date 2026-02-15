package com.virex.wallpapers.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.virex.wallpapers.ui.screens.categories.CategoriesScreen
import com.virex.wallpapers.ui.screens.categories.CategoryDetailScreen
import com.virex.wallpapers.ui.screens.favorites.FavoritesScreen
import com.virex.wallpapers.ui.screens.generator.GeneratorScreen
import com.virex.wallpapers.ui.screens.home.HomeScreen
import com.virex.wallpapers.ui.screens.preview.WallpaperPreviewScreen
import com.virex.wallpapers.ui.screens.pro.ProUpgradeScreen
import com.virex.wallpapers.ui.screens.settings.PrivacyPolicyScreen
import com.virex.wallpapers.ui.screens.settings.SettingsScreen
import com.virex.wallpapers.ui.screens.settings.TermsOfServiceScreen
import com.virex.wallpapers.ui.sync.SyncScreen
import com.virex.wallpapers.ui.sync.SyncedWallpaperDetailScreen
import com.virex.wallpapers.ui.theme.virex.VirexBottomBar
import com.virex.wallpapers.ui.theme.virex.VirexColors
import com.virex.wallpapers.ui.theme.virex.VirexDimens
import com.virex.wallpapers.ui.theme.virex.VirexShapes
import com.virex.wallpapers.ui.premium.PremiumTheme
import com.virex.wallpapers.ui.theme.AmoledBlack
import com.virex.wallpapers.ui.theme.GradientAIEnd
import com.virex.wallpapers.ui.theme.GradientAIMid
import com.virex.wallpapers.ui.theme.GradientAIStart
import com.virex.wallpapers.ui.theme.GradientNeonEnd
import com.virex.wallpapers.ui.theme.GradientNeonStart
import com.virex.wallpapers.ui.theme.NeonBlue
import com.virex.wallpapers.ui.theme.NeonPurple
import com.virex.wallpapers.ui.theme.SurfaceCard
import com.virex.wallpapers.ui.theme.SurfaceGlass
import com.virex.wallpapers.ui.theme.TextSecondary

/**
 * Main Navigation Host
 *
 * Handles all navigation in the app with smooth animations.
 */
@Composable
fun VirexNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Determine if bottom bar should be shown
    val showBottomBar = currentDestination?.route in Screen.bottomNavItems.map { it.route }

    Scaffold(
            containerColor = VirexColors.Background,
            bottomBar = {
                if (showBottomBar) {
                    VirexBottomBar(
                            currentRoute = currentDestination?.route,
                            onNavigate = { route ->
                                android.util.Log.d("VirexNav", "Navigating to: $route from ${currentDestination?.route}")
                                // Use simple navigation for bottom bar items
                                navController.navigate(route) {
                                    // Pop everything up to start destination
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        inclusive = false
                                        saveState = false
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            },
                            onFabClick = { 
                                android.util.Log.d("VirexNav", "FAB clicked, going to generator")
                                navController.navigate(Screen.Generator.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        inclusive = false
                                        saveState = false
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            }
                    )
                }
            }
    ) { innerPadding ->
        NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding),
                enterTransition = {
                    fadeIn(animationSpec = tween(300)) +
                            slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                                    animationSpec = tween(300)
                            )
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(300)) +
                            slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                                    animationSpec = tween(300)
                            )
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(300)) +
                            slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                                    animationSpec = tween(300)
                            )
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(300)) +
                            slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                                    animationSpec = tween(300)
                            )
                }
        ) {
            // Home Screen
            composable(Screen.Home.route) {
                HomeScreen(
                        onWallpaperClick = { wallpaperId ->
                            navController.navigate(Screen.WallpaperPreview.createRoute(wallpaperId))
                        },
                        onCategoryClick = { categoryId ->
                            navController.navigate(Screen.CategoryDetail.createRoute(categoryId))
                        },
                        onProClick = { navController.navigate(Screen.ProUpgrade.route) }
                )
            }

            // Categories Screen
            composable(Screen.Categories.route) {
                CategoriesScreen(
                        onCategoryClick = { categoryId ->
                            navController.navigate(Screen.CategoryDetail.createRoute(categoryId))
                        },
                        onSettingsClick = { navController.navigate(Screen.Settings.route) }
                )
            }

            // Category Detail Screen
            composable(
                    route = Screen.CategoryDetail.route,
                    arguments =
                            listOf(navArgument(NavArgs.CATEGORY_ID) { type = NavType.StringType })
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getString(NavArgs.CATEGORY_ID) ?: ""
                CategoryDetailScreen(
                        categoryId = categoryId,
                        onWallpaperClick = { wallpaperId ->
                            navController.navigate(Screen.WallpaperPreview.createRoute(wallpaperId))
                        },
                        onBackClick = { navController.popBackStack() }
                )
            }

            // Favorites Screen
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                        onWallpaperClick = { wallpaperId ->
                            navController.navigate(Screen.WallpaperPreview.createRoute(wallpaperId))
                        }
                )
            }

            // AI Generator Screen
            composable(Screen.Generator.route) {
                GeneratorScreen(onProClick = { navController.navigate(Screen.ProUpgrade.route) })
            }

            // Auto Sync Screen
            composable(Screen.AutoSync.route) {
                SyncScreen(
                        onWallpaperClick = { wallpaperId ->
                            navController.navigate(
                                    Screen.SyncedWallpaperDetail.createRoute(wallpaperId)
                            )
                        }
                )
            }

            // Synced Wallpaper Detail Screen
            composable(
                    route = Screen.SyncedWallpaperDetail.route,
                    arguments =
                            listOf(
                                    navArgument(NavArgs.SYNCED_WALLPAPER_ID) {
                                        type = NavType.StringType
                                    }
                            )
            ) { backStackEntry ->
                val wallpaperId =
                        backStackEntry.arguments?.getString(NavArgs.SYNCED_WALLPAPER_ID) ?: ""
                SyncedWallpaperDetailScreen(
                        wallpaperId = wallpaperId,
                        onNavigateBack = { navController.popBackStack() }
                )
            }

            // Settings Screen
            composable(Screen.Settings.route) {
                SettingsScreen(
                        onProClick = { navController.navigate(Screen.ProUpgrade.route) },
                        onPrivacyPolicyClick = {
                            navController.navigate(Screen.PrivacyPolicy.route)
                        },
                        onTermsOfServiceClick = {
                            navController.navigate(Screen.TermsOfService.route)
                        }
                )
            }

            // Wallpaper Preview Screen
            composable(
                    route = Screen.WallpaperPreview.route,
                    arguments =
                            listOf(navArgument(NavArgs.WALLPAPER_ID) { type = NavType.StringType })
            ) { backStackEntry ->
                val wallpaperId = backStackEntry.arguments?.getString(NavArgs.WALLPAPER_ID) ?: ""
                WallpaperPreviewScreen(
                        wallpaperId = wallpaperId,
                        onBackClick = { navController.popBackStack() },
                        onProClick = { navController.navigate(Screen.ProUpgrade.route) }
                )
            }

            // PRO Upgrade Screen
            composable(Screen.ProUpgrade.route) {
                ProUpgradeScreen(onBackClick = { navController.popBackStack() })
            }

            // Privacy Policy Screen
            composable(Screen.PrivacyPolicy.route) {
                PrivacyPolicyScreen(onBack = { navController.popBackStack() })
            }

            // Terms of Service Screen
            composable(Screen.TermsOfService.route) {
                TermsOfServiceScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}


