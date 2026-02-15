package com.virex.wallpapers.ui.screens.favorites

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.virex.wallpapers.ui.components.NoFavoritesState
import com.virex.wallpapers.ui.components.WallcraftCard
import com.virex.wallpapers.ui.theme.virex.VirexColors
import com.virex.wallpapers.ui.theme.virex.VirexSpacing
import com.virex.wallpapers.ui.theme.virex.VirexShapes
import com.virex.wallpapers.ui.theme.virex.VirexTypography

/**
 * Favorites Screen
 *
 * Displays user's favorite wallpapers with:
 * - Grid layout
 * - Offline status indicators (PRO)
 * - Swipe to remove
 * - Empty state with helpful message
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
        onWallpaperClick: (String) -> Unit,
        viewModel: FavoritesViewModel = hiltViewModel()
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val cachedIds by viewModel.cachedIds.collectAsStateWithLifecycle()
    val isPro by viewModel.isPro.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val favoritesCount by viewModel.favoritesCount.collectAsStateWithLifecycle()

    Scaffold(
            containerColor = VirexColors.Background,
            topBar = {
                TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                        text = "Favorites",
                                        style = VirexTypography.ScreenTitle,
                                        fontWeight = FontWeight.Bold
                                )
                                
                                // Count badge
                                if (favoritesCount > 0) {
                                    Spacer(modifier = Modifier.width(VirexSpacing.sm))
                                    Box(
                                            modifier = Modifier
                                                    .background(VirexColors.Primary, CircleShape)
                                                    .padding(horizontal = VirexSpacing.sm, vertical = VirexSpacing.xxs)
                                    ) {
                                        Text(
                                                text = favoritesCount.toString(),
                                                style = VirexTypography.Label,
                                                fontWeight = FontWeight.Bold,
                                                color = VirexColors.Background
                                        )
                                    }
                                }
                            }
                        },
                        actions = {
                            // PRO offline status
                            if (isPro && favorites.isNotEmpty()) {
                                val cachedCount = favorites.count { cachedIds.contains(it.id) }
                                Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                                .background(VirexColors.Surface, RoundedCornerShape(VirexShapes.radiusSm))
                                                .padding(horizontal = VirexSpacing.sm, vertical = VirexSpacing.xxs)
                                ) {
                                    Icon(
                                            imageVector = if (cachedCount == favorites.size) 
                                                    Icons.Default.CloudDone else Icons.Default.CloudOff,
                                            contentDescription = "Offline status",
                                            tint = if (cachedCount == favorites.size) VirexColors.Success else VirexColors.TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(VirexSpacing.xxs))
                                    Text(
                                            text = "$cachedCount/${favorites.size}",
                                            style = VirexTypography.Label,
                                            color = VirexColors.TextSecondary
                                    )
                                }
                            }
                        },
                        colors =
                                TopAppBarDefaults.topAppBarColors(
                                        containerColor = VirexColors.Background,
                                        titleContentColor = VirexColors.TextPrimary
                                )
                )
            }
    ) { paddingValues ->
        AnimatedVisibility(
                visible = favorites.isEmpty() && !isLoading,
                enter = fadeIn(),
                exit = fadeOut()
        ) {
            NoFavoritesState(modifier = Modifier.padding(paddingValues))
        }
        
        AnimatedVisibility(
                visible = favorites.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                // PRO offline info banner
                if (isPro) {
                    Box(
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = VirexSpacing.sm, vertical = VirexSpacing.sm)
                                    .background(VirexColors.Surface, RoundedCornerShape(VirexShapes.radiusSm))
                                    .padding(VirexSpacing.sm)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = VirexColors.ProGold,
                                    modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(VirexSpacing.sm))
                            Text(
                                    text = "PRO: Your favorites are available offline",
                                    style = VirexTypography.Caption,
                                    color = VirexColors.TextSecondary
                            )
                        }
                    }
                }
                
                LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(VirexSpacing.sm),
                        horizontalArrangement = Arrangement.spacedBy(VirexSpacing.sm),
                        verticalArrangement = Arrangement.spacedBy(VirexSpacing.sm)
                ) {
                    items(
                            items = favorites, 
                            key = { it.id }
                    ) { wallpaper ->
                        AnimatedVisibility(
                                visible = true,
                                enter = scaleIn(
                                        animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                        )
                                ) + fadeIn(),
                                exit = scaleOut() + fadeOut()
                        ) {
                            Box {
                                WallcraftCard(
                                        wallpaper = wallpaper,
                                        isFavorite = favoriteIds.contains(wallpaper.id),
                                        isPro = isPro,
                                        onClick = { onWallpaperClick(wallpaper.id) },
                                        onFavoriteClick = { viewModel.toggleFavorite(wallpaper.id) },
                                        modifier = Modifier.height(280.dp)
                                )
                                
                                // Cached indicator for PRO users
                                if (isPro && cachedIds.contains(wallpaper.id)) {
                                    Box(
                                            modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(VirexSpacing.sm)
                                                    .size(20.dp)
                                                    .background(VirexColors.Success, CircleShape),
                                            contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                                imageVector = Icons.Default.CloudDone,
                                                contentDescription = "Available offline",
                                                tint = VirexColors.Background,
                                                modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
