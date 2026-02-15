package com.virex.wallpapers.ui.premium.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.virex.wallpapers.data.model.Category
import com.virex.wallpapers.data.model.Wallpaper
import com.virex.wallpapers.ui.premium.PremiumTheme
import com.virex.wallpapers.ui.premium.components.PremiumShimmer
import com.virex.wallpapers.ui.premium.components.PremiumWallpaperCard
import kotlinx.coroutines.delay

/**
 * Premium Home Screen - Flagship Experience
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PremiumHomeScreen(
    wallpapers: List<Wallpaper>,
    categories: List<Category>,
    favorites: Set<String>,
    isPro: Boolean,
    isLoading: Boolean,
    onWallpaperClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onProClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var isSearchFocused by remember { mutableStateOf(false) }
    
    // Featured wallpapers (first 5)
    val featuredWallpapers = remember(wallpapers) { wallpapers.take(5) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PremiumTheme.Background)
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(
                start = 12.dp,
                end = 12.dp,
                top = 0.dp,
                bottom = 100.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp
        ) {
            // ═══════════════════════════════════════════════════════════════
            // DYNAMIC HEADER
            // ═══════════════════════════════════════════════════════════════
            item(span = StaggeredGridItemSpan.FullLine) {
                DynamicHeader(
                    isPro = isPro,
                    onProClick = onProClick
                )
            }
            
            // ═══════════════════════════════════════════════════════════════
            // SMART SEARCH BAR
            // ═══════════════════════════════════════════════════════════════
            item(span = StaggeredGridItemSpan.FullLine) {
                GlassSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    isFocused = isSearchFocused,
                    onFocusChange = { isSearchFocused = it },
                    onSearchClick = onSearchClick
                )
            }
            
            // ═══════════════════════════════════════════════════════════════
            // HORIZONTAL LIVE CATEGORIES
            // ═══════════════════════════════════════════════════════════════
            item(span = StaggeredGridItemSpan.FullLine) {
                if (categories.isNotEmpty()) {
                    LiveCategoriesSection(
                        categories = categories,
                        onCategoryClick = onCategoryClick
                    )
                }
            }
            
            // ═══════════════════════════════════════════════════════════════
            // FEATURED CAROUSEL
            // ═══════════════════════════════════════════════════════════════
            item(span = StaggeredGridItemSpan.FullLine) {
                if (featuredWallpapers.isNotEmpty()) {
                    FeaturedCarousel(
                        wallpapers = featuredWallpapers,
                        onWallpaperClick = onWallpaperClick
                    )
                }
            }
            
            // ═══════════════════════════════════════════════════════════════
            // SECTION HEADER
            // ═══════════════════════════════════════════════════════════════
            item(span = StaggeredGridItemSpan.FullLine) {
                SectionHeader(
                    title = "Explore",
                    subtitle = "${wallpapers.size} wallpapers"
                )
            }
            
            // ═══════════════════════════════════════════════════════════════
            // STAGGERED WALLPAPER GRID
            // ═══════════════════════════════════════════════════════════════
            if (isLoading) {
                items(8) { index ->
                    PremiumShimmer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (index % 3 == 0) 320.dp else 240.dp)
                            .clip(RoundedCornerShape(PremiumTheme.CornerRadiusLarge))
                    )
                }
            } else {
                itemsIndexed(
                    items = wallpapers,
                    key = { _, wp -> wp.id }
                ) { index, wallpaper ->
                    // Staggered heights for visual interest
                    val height = when (index % 5) {
                        0 -> 320.dp
                        1, 3 -> 240.dp
                        else -> 280.dp
                    }
                    
                    PremiumWallpaperCard(
                        wallpaper = wallpaper,
                        isFavorite = favorites.contains(wallpaper.id),
                        isPro = isPro,
                        onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onWallpaperClick(wallpaper.id) 
                        },
                        onFavoriteClick = { onFavoriteClick(wallpaper.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(height)
                            .animateItemPlacement()
                    )
                }
            }
        }
    }
}

/**
 * Dynamic Header with animated gradient text
 */
@Composable
fun DynamicHeader(
    isPro: Boolean,
    onProClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "header")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradientOffset"
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Discover",
                    style = MaterialTheme.typography.titleMedium,
                    color = PremiumTheme.TextSecondary
                )
                
                // Animated gradient text
                Text(
                    text = "VIREX",
                    style = MaterialTheme.typography.displayMedium.copy(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                PremiumTheme.NeonBlue,
                                PremiumTheme.NeonPurple,
                                PremiumTheme.NeonPink,
                                PremiumTheme.NeonBlue
                            ),
                            start = Offset(gradientOffset, 0f),
                            end = Offset(gradientOffset + 300f, 100f)
                        )
                    ),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )
            }
            
            // PRO badge or upgrade button
            if (isPro) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    PremiumTheme.ProGradientStart,
                                    PremiumTheme.ProGradientEnd
                                )
                            ),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "PRO",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            } else {
                IconButton(
                    onClick = onProClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(PremiumTheme.GlassPrimary, CircleShape)
                        .border(1.dp, PremiumTheme.GlassBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Upgrade to PRO",
                        tint = PremiumTheme.ProGold
                    )
                }
            }
        }
    }
}

/**
 * Glass-morphism search bar with morph animation
 */
@Composable
fun GlassSearchBar(
    query: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    isFocused: Boolean,
    onFocusChange: (Boolean) -> Unit,
    onSearchClick: () -> Unit
) {
    val animatedHeight by animateDpAsState(
        targetValue = if (isFocused) 60.dp else 52.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "height"
    )
    
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isFocused) PremiumTheme.NeonBlue else PremiumTheme.GlassBorder,
        animationSpec = tween(300),
        label = "borderColor"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(animatedHeight)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        PremiumTheme.GlassPrimary,
                        PremiumTheme.GlassSecondary,
                        PremiumTheme.GlassPrimary
                    )
                ),
                RoundedCornerShape(PremiumTheme.CornerRadiusMedium)
            )
            .border(
                1.dp,
                animatedBorderColor,
                RoundedCornerShape(PremiumTheme.CornerRadiusMedium)
            )
            .clickable { onSearchClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = PremiumTheme.TextSecondary,
                modifier = Modifier.size(22.dp)
            )
            
            Text(
                text = "Search wallpapers...",
                style = MaterialTheme.typography.bodyLarge,
                color = PremiumTheme.TextTertiary
            )
        }
    }
}

/**
 * Horizontal scrolling live categories with icons
 */
@Composable
fun LiveCategoriesSection(
    categories: List<Category>,
    onCategoryClick: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        SectionHeader(
            title = "Categories",
            subtitle = null
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(categories, key = { it.id }) { category ->
                LiveCategoryCard(
                    category = category,
                    onClick = { onCategoryClick(category.id) }
                )
            }
        }
    }
}

/**
 * Category card with background image and icon
 */
@Composable
fun LiveCategoryCard(
    category: Category,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    Box(
        modifier = Modifier
            .width(140.dp)
            .height(PremiumTheme.CategoryCardHeight)
            .clip(RoundedCornerShape(PremiumTheme.CornerRadiusMedium))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
    ) {
        // Background image
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(category.coverUrl)
                .crossfade(300)
                .build(),
            contentDescription = category.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = {
                PremiumShimmer(modifier = Modifier.fillMaxSize())
            }
        )
        
        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
        )
        
        // Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "${category.wallpaperCount}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Auto-sliding featured carousel with parallax effect
 */
@Composable
fun FeaturedCarousel(
    wallpapers: List<Wallpaper>,
    onWallpaperClick: (String) -> Unit
) {
    val listState = rememberLazyListState()
    var currentIndex by remember { mutableStateOf(0) }
    
    // Auto scroll effect
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            val nextIndex = (currentIndex + 1) % wallpapers.size
            listState.animateScrollToItem(nextIndex)
            currentIndex = nextIndex
        }
    }
    
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        SectionHeader(
            title = "Featured",
            subtitle = "Editor's picks"
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            itemsIndexed(wallpapers, key = { _, wp -> wp.id }) { index, wallpaper ->
                FeaturedCard(
                    wallpaper = wallpaper,
                    onClick = { onWallpaperClick(wallpaper.id) }
                )
            }
        }
        
        // Page indicator
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            wallpapers.forEachIndexed { index, _ ->
                val isSelected = index == currentIndex
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (isSelected) 8.dp else 6.dp)
                        .background(
                            if (isSelected) PremiumTheme.NeonBlue else PremiumTheme.TextTertiary,
                            CircleShape
                        )
                )
            }
        }
    }
}

/**
 * Featured wallpaper card with scale animation
 */
@Composable
fun FeaturedCard(
    wallpaper: Wallpaper,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .width(300.dp)
            .height(PremiumTheme.FeaturedCardHeight)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(16.dp, RoundedCornerShape(PremiumTheme.CornerRadiusLarge), spotColor = PremiumTheme.NeonPurple.copy(alpha = 0.3f))
            .clip(RoundedCornerShape(PremiumTheme.CornerRadiusLarge))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    }
                )
            }
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(wallpaper.fullUrl)
                .crossfade(400)
                .build(),
            contentDescription = wallpaper.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = {
                PremiumShimmer(modifier = Modifier.fillMaxSize())
            }
        )
        
        // Gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
        )
        
        // Title
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        ) {
            Text(
                text = wallpaper.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = wallpaper.categoryName,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Section header with title and subtitle
 */
@Composable
fun SectionHeader(
    title: String,
    subtitle: String?
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = PremiumTheme.TextPrimary
        )
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = PremiumTheme.TextSecondary
            )
        }
    }
}
