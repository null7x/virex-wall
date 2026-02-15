package com.virex.wallpapers.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.virex.wallpapers.ads.VkAdsBanner
import com.virex.wallpapers.data.model.Category
import com.virex.wallpapers.data.model.RecommendedWallpaper
import com.virex.wallpapers.data.model.UiState
import com.virex.wallpapers.data.model.Wallpaper
import com.virex.wallpapers.ui.components.CategoryCard
import com.virex.wallpapers.ui.components.SectionHeader
import com.virex.wallpapers.ui.components.ShimmerBox
import com.virex.wallpapers.ui.components.WallcraftCard
import com.virex.wallpapers.ui.components.WallcraftFilterChip
import com.virex.wallpapers.ui.components.WallcraftStatsCard
import com.virex.wallpapers.ui.theme.virex.VirexColors
import com.virex.wallpapers.ui.theme.virex.VirexShapes
import com.virex.wallpapers.ui.theme.virex.VirexSpacing
import com.virex.wallpapers.ui.theme.virex.VirexTypography
import com.virex.wallpapers.ui.theme.virex.VirexDimens

/**
 * Home Screen
 *
 * Main screen with featured, trending, and new wallpapers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
        onWallpaperClick: (String) -> Unit,
        onCategoryClick: (String) -> Unit,
        onProClick: () -> Unit,
        viewModel: HomeViewModel = hiltViewModel()
) {
    val allState by viewModel.allWallpapers.collectAsStateWithLifecycle()
    val categoriesState by viewModel.categories.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val isPro by viewModel.isPro.collectAsStateWithLifecycle()
    var selectedCategoryId by rememberSaveable { mutableStateOf("all") }

    val allWallpapers = (allState as? UiState.Success)?.data.orEmpty()
    val chipCategories =
            remember(categoriesState, allWallpapers) {
                val fromBackend = (categoriesState as? UiState.Success)?.data.orEmpty()
                if (fromBackend.isNotEmpty()) {
                    fromBackend
                } else {
                    allWallpapers
                            .groupBy { it.categoryId.ifBlank { it.categoryName.lowercase() } }
                            .map { (id, items) ->
                                Category(
                                        id = id,
                                        name = items.firstOrNull()?.categoryName ?: id,
                                        wallpaperCount = items.size
                                )
                            }
                }
            }

    val filteredWallpapers =
            remember(allWallpapers, selectedCategoryId) {
                if (selectedCategoryId == "all") allWallpapers
                else allWallpapers.filter { wp ->
                    wp.categoryId.equals(selectedCategoryId, ignoreCase = true) ||
                            wp.categoryName.equals(selectedCategoryId, ignoreCase = true)
                }
            }

    Column(modifier = Modifier.fillMaxWidth().background(VirexColors.Background)) {
        TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Hexagon V Logo
                        Box(
                                modifier = Modifier
                                        .size(VirexDimens.logoSize)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(VirexColors.PrimaryGradient),
                                contentAlignment = Alignment.Center
                        ) {
                            Text(
                                    text = "V",
                                    style = VirexTypography.CardTitle,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.size(VirexSpacing.xs))
                        Text(
                                text = "VIREX",
                                style = VirexTypography.ScreenTitle,
                                fontWeight = FontWeight.Black,
                                color = VirexColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.size(VirexSpacing.xs))
                        Surface(
                                shape = RoundedCornerShape(VirexShapes.chip),
                                color = if (isPro) VirexColors.ProGold.copy(alpha = 0.2f) else VirexColors.GlassPrimary
                        ) {
                            Text(
                                    text = if (isPro) "PRO" else "FREE",
                                    style = VirexTypography.Label,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPro) VirexColors.ProGold else VirexColors.Primary,
                                    modifier = Modifier.padding(horizontal = VirexSpacing.sm, vertical = VirexSpacing.xxs)
                            )
                        }
                    }
                },
                actions = {
                    if (!isPro) {
                        IconButton(onClick = onProClick) {
                            Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Upgrade",
                                    tint = VirexColors.ProGold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VirexColors.Background)
        )

        PullToRefreshBox(
                isRefreshing = allState is UiState.Loading,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxWidth()
        ) {
            LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item(span = { GridItemSpan(2) }) {
                    Box(
                            modifier =
                                    Modifier.fillMaxWidth()
                                            .clip(RoundedCornerShape(VirexShapes.radiusMd))
                                            .background(VirexColors.GlassPrimary)
                                            .border(
                                                    1.dp,
                                                    VirexColors.GlassBorder,
                                                    RoundedCornerShape(VirexShapes.radiusMd)
                                            )
                                            .padding(VirexSpacing.md)
                    ) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                        text = "Explore",
                                        style = VirexTypography.Label,
                                        color = VirexColors.TextSecondary
                                )
                                Text(
                                        text = "${allWallpapers.size} wallpapers",
                                        style = VirexTypography.CardTitle,
                                        color = VirexColors.TextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                )
                            }
                            Icon(
                                    imageVector = Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    tint = VirexColors.Primary
                            )
                        }
                    }
                }

                item(span = { GridItemSpan(2) }) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        item {
                            WallcraftFilterChip(
                                    selected = selectedCategoryId == "all",
                                    onClick = { selectedCategoryId = "all" },
                                    label = "All",
                                    count = allWallpapers.size
                            )
                        }
                        items(chipCategories, key = { it.id }) { category ->
                            WallcraftFilterChip(
                                    selected = selectedCategoryId == category.id,
                                    onClick = { selectedCategoryId = category.id },
                                    label = category.name,
                                    count = category.wallpaperCount
                            )
                        }
                    }
                }

                when (allState) {
                    is UiState.Loading -> {
                        items(8) {
                            ShimmerBox(
                                    modifier = Modifier.fillMaxWidth().height(280.dp)
                            )
                        }
                    }
                    is UiState.Error -> {
                        item(span = { GridItemSpan(2) }) {
                            Text(
                                    text = (allState as UiState.Error).message,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    else -> {
                        if (filteredWallpapers.isEmpty()) {
                            item(span = { GridItemSpan(2) }) {
                                Text(
                                        text = "No wallpapers in this category",
                                        color = VirexColors.TextPrimary.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(VirexSpacing.md)
                                )
                            }
                        } else {
                            items(filteredWallpapers, key = { it.id }) { wallpaper ->
                                WallcraftCard(
                                        wallpaper = wallpaper,
                                        isFavorite = favorites.contains(wallpaper.id),
                                        isPro = isPro,
                                        onClick = {
                                            viewModel.trackView(wallpaper)
                                            if (wallpaper.isPremium && !isPro) {
                                                onProClick()
                                            } else {
                                                onWallpaperClick(wallpaper.id)
                                            }
                                        },
                                        onFavoriteClick = { viewModel.toggleFavorite(wallpaper.id) },
                                        modifier = Modifier.fillMaxWidth().height(280.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (!isPro) {
            VkAdsBanner(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun WallpaperHorizontalList(
        state: UiState<List<Wallpaper>>,
        favorites: Set<String>,
        isPro: Boolean,
        onWallpaperClick: (String) -> Unit,
        onFavoriteClick: (String) -> Unit,
        onRetry: () -> Unit
) {
    when (state) {
        is UiState.Loading -> {
            LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(5) {
                    ShimmerBox(
                            modifier = Modifier.fillParentMaxWidth(0.4f).height(280.dp)
                    )
                }
            }
        }
        is UiState.Success -> {
            LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(state.data) { wallpaper ->
                    WallcraftCard(
                            wallpaper = wallpaper,
                            isFavorite = favorites.contains(wallpaper.id),
                            isPro = isPro,
                            onClick = { onWallpaperClick(wallpaper.id) },
                            onFavoriteClick = { onFavoriteClick(wallpaper.id) },
                            modifier = Modifier.fillParentMaxWidth(0.4f).height(280.dp)
                    )
                }
            }
        }
        is UiState.Error -> {
            Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
            ) {
                Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                )
            }
        }
        is UiState.Empty -> {
            Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
            ) {
                Text(
                        text = "No wallpapers found",
                        style = VirexTypography.Body,
                        color = VirexColors.TextPrimary.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/** Section for recommended wallpapers with reason badges */
@Composable
private fun RecommendedSection(
        title: String,
        state: UiState<List<RecommendedWallpaper>>,
        favorites: Set<String>,
        isPro: Boolean,
        onWallpaperClick: (String) -> Unit,
        onFavoriteClick: (String) -> Unit
) {
    when (state) {
        is UiState.Loading -> {
            // Don't show shimmer for recommendations, just skip
        }
        is UiState.Success -> {
            if (state.data.isNotEmpty()) {
                SectionHeader(title = title)
                LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(state.data) { recommended ->
                        RecommendedWallpaperCard(
                                recommended = recommended,
                                isFavorite = favorites.contains(recommended.wallpaper.id),
                                isPro = isPro,
                                onClick = { onWallpaperClick(recommended.wallpaper.id) },
                                onFavoriteClick = { onFavoriteClick(recommended.wallpaper.id) },
                                modifier = Modifier.fillParentMaxWidth(0.4f).height(300.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        is UiState.Error, is UiState.Empty -> {
            // Hide recommendations section if no data
        }
    }
}

/** Wallpaper card with recommendation reason badge */
@Composable
private fun RecommendedWallpaperCard(
        recommended: RecommendedWallpaper,
        isFavorite: Boolean,
        isPro: Boolean,
        onClick: () -> Unit,
        onFavoriteClick: () -> Unit,
        modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        WallcraftCard(
                wallpaper = recommended.wallpaper,
                isFavorite = isFavorite,
                isPro = isPro,
                onClick = onClick,
                onFavoriteClick = onFavoriteClick,
                modifier = Modifier.weight(1f).fillMaxWidth()
        )
        // Recommendation reason badge
        Text(
                text = recommended.reasonText,
                style = VirexTypography.Label,
                color = VirexColors.PrimaryVariant,
                modifier = Modifier.fillMaxWidth()
        )
    }
}
