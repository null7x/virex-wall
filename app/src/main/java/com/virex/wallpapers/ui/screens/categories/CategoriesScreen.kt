package com.virex.wallpapers.ui.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SlowMotionVideo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.virex.wallpapers.data.model.Category
import com.virex.wallpapers.data.model.UiState
import com.virex.wallpapers.ui.theme.virex.VirexColors
import com.virex.wallpapers.ui.theme.virex.VirexSpacing
import com.virex.wallpapers.ui.theme.virex.VirexShapes
import com.virex.wallpapers.ui.theme.virex.VirexTypography
import com.virex.wallpapers.ui.theme.virex.VirexDimens

/**
 * Categories Screen
 *
 * Displays all available wallpaper categories.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
        onCategoryClick: (String) -> Unit,
        onSettingsClick: () -> Unit = {},
        viewModel: CategoriesViewModel = hiltViewModel()
) {
    val categoriesState by viewModel.categories.collectAsStateWithLifecycle()
    val isPro by viewModel.isPro.collectAsStateWithLifecycle()
    var mode by rememberSaveable { mutableStateOf(CategoryMode.RECENT) }

    val allCategories = (categoriesState as? UiState.Success)?.data.orEmpty()
    val visibleCategories =
            remember(allCategories, mode) {
                when (mode) {
                    CategoryMode.RECENT -> allCategories.sortedBy { it.sortOrder }
                    CategoryMode.STOCK ->
                            allCategories
                                    .filter { !it.isPremium }
                                    .ifEmpty { allCategories }
                    CategoryMode.LIVE ->
                            allCategories
                                    .sortedByDescending { it.wallpaperCount }
                                    .ifEmpty { allCategories }
                }
            }

    Column(
            modifier =
                    Modifier.fillMaxSize().background(VirexColors.Background).padding(horizontal = VirexSpacing.md)
    ) {
        SpacerBlock(14)

        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Logo V
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
                SpacerBlock(width = 10)
                Text(
                        text = "VIREX",
                        style = VirexTypography.ScreenTitle,
                        fontWeight = FontWeight.Black,
                        color = VirexColors.TextPrimary
                )
                SpacerBlock(width = 8)
                Surface(shape = RoundedCornerShape(VirexShapes.chip), color = if (isPro) VirexColors.ProGold.copy(alpha = 0.2f) else VirexColors.GlassPrimary) {
                    Text(
                            text = if (isPro) "PRO" else "FREE",
                            color = if (isPro) VirexColors.ProGold else VirexColors.Primary,
                            style = VirexTypography.Label,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = VirexSpacing.sm, vertical = VirexSpacing.xxs)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(VirexSpacing.xs)) {
                TopCircleButton(icon = Icons.Outlined.Search, onClick = {})
                TopCircleButton(icon = Icons.Outlined.Settings, onClick = onSettingsClick)
            }
        }

        SpacerBlock(16)

        Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(VirexShapes.radiusLg),
                color = VirexColors.SurfaceElevated
        ) {
            Row(
                    modifier = Modifier.fillMaxWidth().padding(VirexSpacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(VirexSpacing.sm)
            ) {
                ModeChip(
                        text = "Последн",
                        icon = Icons.Outlined.AccessTime,
                        selected = mode == CategoryMode.RECENT,
                        onClick = { mode = CategoryMode.RECENT },
                        modifier = Modifier.weight(1f)
                )
                ModeChip(
                        text = "Сток",
                        icon = Icons.Outlined.Image,
                        selected = mode == CategoryMode.STOCK,
                        onClick = { mode = CategoryMode.STOCK },
                        modifier = Modifier.weight(1f)
                )
                ModeChip(
                        text = "Живые",
                        icon = Icons.Outlined.SlowMotionVideo,
                        selected = mode == CategoryMode.LIVE,
                        onClick = { mode = CategoryMode.LIVE },
                        modifier = Modifier.weight(1f)
                )
            }
        }

        SpacerBlock(14)

        when (categoriesState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Загрузка категорий...", color = VirexColors.TextPrimary.copy(alpha = 0.8f))
                }
            }
            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Ошибка загрузки", color = VirexColors.Error)
                }
            }
            is UiState.Empty -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Категории не найдены", color = VirexColors.TextPrimary.copy(alpha = 0.8f))
                }
            }
            is UiState.Success -> {
                LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(VirexSpacing.sm),
                        horizontalArrangement = Arrangement.spacedBy(VirexSpacing.sm),
                        contentPadding = PaddingValues(bottom = VirexSpacing.lg)
                ) {
                    items(visibleCategories, key = { it.id }) { category ->
                        CategoryGridCard(
                                category = category,
                                isPremiumLocked = category.isPremium && !isPro,
                                onClick = { onCategoryClick(category.id) }
                        )
                    }
                }
            }
        }
    }
}

private enum class CategoryMode {
    RECENT,
    STOCK,
    LIVE
}

@Composable
private fun ModeChip(
        text: String,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
) {
    val selectedBrush = VirexColors.PrimaryGradient

    Surface(
            modifier = modifier.clip(RoundedCornerShape(VirexShapes.radiusLg)).clickable(onClick = onClick),
            shape = RoundedCornerShape(VirexShapes.radiusLg),
            color = if (selected) Color.Transparent else VirexColors.SurfaceElevated
    ) {
        Box(
                modifier =
                        if (selected) Modifier.background(selectedBrush).padding(vertical = VirexSpacing.sm)
                        else Modifier.padding(vertical = VirexSpacing.sm)
        ) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                SpacerBlock(width = 6)
                Text(text = text, color = Color.White, style = VirexTypography.Label)
            }
        }
    }
}

@Composable
private fun CategoryGridCard(
        category: Category,
        isPremiumLocked: Boolean,
        onClick: () -> Unit
) {
    Box(
            modifier =
                    Modifier.fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(VirexShapes.radiusLg))
                            .clickable(onClick = onClick)
    ) {
        AsyncImage(
                model = category.coverUrl,
                contentDescription = category.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
        )

        Box(
                modifier =
                        Modifier.fillMaxSize()
                                .background(
                                        Brush.horizontalGradient(
                                                colors =
                                                        listOf(
                                                                Color(0xA0000000),
                                                                Color(0x50000000),
                                                                Color(0x15000000)
                                                        )
                                        )
                                )
        )

        Text(
                text = category.name,
                color = Color.White,
                style = VirexTypography.SectionTitle,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.BottomStart).padding(VirexSpacing.md)
        )

        Surface(
                modifier = Modifier.align(Alignment.BottomEnd).padding(VirexSpacing.sm),
                shape = RoundedCornerShape(VirexShapes.chip),
                color = VirexColors.GlassPrimary
        ) {
            Text(
                    text =
                            if (isPremiumLocked) {
                                "PRO"
                            } else {
                                category.wallpaperCount.toString()
                            },
                    color = Color.White,
                    style = VirexTypography.CardTitle,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = VirexSpacing.sm, vertical = VirexSpacing.xs)
            )
        }
    }
}

@Composable
private fun TopCircleButton(
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        onClick: () -> Unit
) {
    Surface(shape = RoundedCornerShape(VirexShapes.chip), color = VirexColors.SurfaceElevated) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = null, tint = VirexColors.TextPrimary)
        }
    }
}

@Composable
private fun SpacerBlock(height: Int? = null, width: Int? = null) {
    when {
        height != null -> androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(height.dp))
        width != null -> androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(width.dp, 1.dp))
    }
}
