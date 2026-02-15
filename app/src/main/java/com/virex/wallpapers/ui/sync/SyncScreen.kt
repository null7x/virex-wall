package com.virex.wallpapers.ui.sync

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Castle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Gradient
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Texture
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.Brightness2
import androidx.compose.material.icons.outlined.Minimize
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.virex.wallpapers.data.model.SyncCategory
import com.virex.wallpapers.data.model.SyncedWallpaper
import com.virex.wallpapers.data.model.WallpaperSource
import com.virex.wallpapers.ui.theme.AmoledBlack
import com.virex.wallpapers.ui.theme.NeonBlue
import com.virex.wallpapers.ui.theme.SurfaceCard
import com.virex.wallpapers.ui.theme.SurfaceGlass
import com.virex.wallpapers.ui.theme.TextPrimary
import com.virex.wallpapers.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Auto Sync Screen - Premium Design with modern glassmorphism UI */
@Composable
fun SyncScreen(onWallpaperClick: (String) -> Unit, viewModel: SyncViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val allWallpapers by viewModel.allWallpapers.collectAsStateWithLifecycle()
    val newWallpapers by viewModel.newWallpapers.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val unviewedCount by viewModel.unviewedCount.collectAsStateWithLifecycle()
    val categoryCounts =
            remember(allWallpapers) {
                SyncCategory.values().toList().associateWith { category ->
                    allWallpapers.count { it.category == category }
                }
            }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show messages
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // Show errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar("Error: $it")
            viewModel.clearError()
        }
    }

    Box(
            modifier =
                    Modifier.fillMaxSize()
                            .background(
                                    Brush.verticalGradient(
                                            colors =
                                                    listOf(
                                                            Color(0xFF0A1525),
                                                            Color(0xFF050A15),
                                                            AmoledBlack
                                                    )
                                    )
                            )
    ) {
        Scaffold(
                containerColor = Color.Transparent,
                snackbarHost = {
                    SnackbarHost(snackbarHostState) { data ->
                        Snackbar(
                                snackbarData = data,
                                containerColor = SurfaceCard,
                                contentColor = TextPrimary,
                                actionColor = NeonBlue,
                                shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Premium Header
                PremiumSyncHeader(
                        isSyncing = uiState.isSyncing,
                        unviewedCount = unviewedCount,
                        totalWallpapers = allWallpapers.size,
                        status = syncStatus,
                        onSyncClick = { viewModel.triggerSync() }
                )

                // Sync progress indicator
                AnimatedVisibility(visible = uiState.isSyncing) {
                    LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = NeonBlue,
                            trackColor = Color(0xFF1A2540)
                    )
                }

                // Category filter chips
                PremiumCategoryFilterRow(
                        selectedCategory = selectedCategory,
                        totalCount = allWallpapers.size,
                        categoryCounts = categoryCounts,
                        onCategorySelected = { viewModel.selectCategory(it) }
                )

                // Wallpapers grid
                if (allWallpapers.isEmpty() && !uiState.isSyncing) {
                    PremiumEmptyState(onSyncClick = { viewModel.triggerSync() })
                } else {
                    WallpaperGrid(
                            wallpapers =
                                    if (selectedCategory == null) allWallpapers
                                    else {
                                        allWallpapers.filter { it.category == selectedCategory }
                                    },
                            newWallpapers = newWallpapers,
                            onWallpaperClick = { wallpaper ->
                                viewModel.markAsViewed(wallpaper.id)
                                onWallpaperClick(wallpaper.id)
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumSyncHeader(
        isSyncing: Boolean,
        unviewedCount: Int,
        totalWallpapers: Int,
        status: com.virex.wallpapers.data.model.SyncStatus?,
        onSyncClick: () -> Unit
) {
    val lastSyncText =
            if (status?.lastSyncAt != null && status.lastSyncAt > 0) {
                val formatter = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                formatter.format(Date(status.lastSyncAt))
            } else {
                "Never"
            }

    val rotationAngle by
            animateFloatAsState(
                    targetValue = if (isSyncing) 360f else 0f,
                    animationSpec = tween(1000),
                    label = "syncRotation"
            )

    Card(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Box(
                modifier =
                        Modifier.fillMaxWidth()
                                .background(
                                        Brush.horizontalGradient(
                                                listOf(
                                                        Color(0xFF0D1A30),
                                                        Color(0xFF0A1525),
                                                        Color(0xFF061020)
                                                )
                                        )
                                )
                                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Sync Icon with glow
                        Box(
                                modifier =
                                        Modifier.size(48.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(
                                                        Brush.linearGradient(
                                                                colors =
                                                                        listOf(
                                                                                Color(0xFF06B6D4),
                                                                                Color(0xFF3B82F6)
                                                                        )
                                                        )
                                                ),
                                contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                    text = "Auto Sync",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                            )
                            Text(
                                    text = "Fresh wallpapers daily",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                            )
                        }
                    }

                    // Sync button with animation
                    Box(
                            modifier =
                                    Modifier.size(44.dp)
                                            .clip(CircleShape)
                                            .background(
                                                    if (isSyncing) NeonBlue.copy(alpha = 0.2f)
                                                    else SurfaceGlass
                                            )
                                            .border(1.dp, NeonBlue.copy(alpha = 0.3f), CircleShape)
                                            .clickable(enabled = !isSyncing) { onSyncClick() },
                            contentAlignment = Alignment.Center
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                    color = NeonBlue
                            )
                        } else {
                            Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Sync",
                                    tint = NeonBlue,
                                    modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Stats row
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    // Last sync
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                                modifier =
                                        Modifier.size(6.dp)
                                                .clip(CircleShape)
                                                .background(
                                                        if (isSyncing) NeonBlue
                                                        else Color(0xFF10B981)
                                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                                text = "Last sync: $lastSyncText",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                        )
                    }

                    // Total wallpapers
                    Text(
                            text = "$totalWallpapers wallpapers",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                    )
                }

                // New unseen badge
                if (unviewedCount > 0) {
                    Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors =
                                    CardDefaults.cardColors(
                                            containerColor = NeonBlue.copy(alpha = 0.15f)
                                    ),
                            border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.3f))
                    ) {
                        Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "✨", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                    text = "$unviewedCount new wallpapers to explore",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = NeonBlue,
                                    fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumCategoryFilterRow(
        selectedCategory: SyncCategory?,
        totalCount: Int,
        categoryCounts: Map<SyncCategory, Int>,
        onCategorySelected: (SyncCategory?) -> Unit
) {
    LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // All filter
        item {
            PremiumFilterChip(
                    selected = selectedCategory == null,
                    text = "All ($totalCount)",
                    icon = Icons.Default.FilterAlt,
                    onClick = { onCategorySelected(null) }
            )
        }

        // Category filters
        items(SyncCategory.values().toList()) { category ->
            PremiumFilterChip(
                    selected = selectedCategory == category,
                    text = "${category.displayName} (${categoryCounts[category] ?: 0})",
                    icon = category.icon,
                    onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
private fun PremiumFilterChip(
        selected: Boolean,
        text: String,
        icon: ImageVector,
        onClick: () -> Unit
) {
    val scale by
            animateFloatAsState(
                    targetValue = if (selected) 1.05f else 1f,
                    animationSpec = tween(150),
                    label = "chipScale"
            )

    Card(
            modifier = Modifier.scale(scale).clickable { onClick() },
            shape = RoundedCornerShape(12.dp),
            colors =
                    CardDefaults.cardColors(
                            containerColor =
                                    if (selected) NeonBlue.copy(alpha = 0.2f) else SurfaceGlass
                    ),
            border =
                    BorderStroke(
                            1.dp,
                            if (selected) NeonBlue.copy(alpha = 0.5f)
                            else Color.White.copy(alpha = 0.08f)
                    )
    ) {
        Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) NeonBlue else TextSecondary,
                    modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) NeonBlue else TextSecondary,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

private val SyncCategory.icon: ImageVector
    get() =
            when (this) {
                SyncCategory.AMOLED -> Icons.Default.DarkMode
                SyncCategory.DARK -> Icons.Outlined.Brightness2
                SyncCategory.MINIMAL -> Icons.Outlined.Minimize
                SyncCategory.NATURE -> Icons.Default.Park
                SyncCategory.SPACE -> Icons.Default.RocketLaunch
                SyncCategory.ABSTRACT -> Icons.Default.AutoAwesome
                SyncCategory.ANIME -> Icons.Default.Face
                SyncCategory.CYBERPUNK -> Icons.Default.Memory
                SyncCategory.CARS -> Icons.Default.DirectionsCar
                SyncCategory.CITY -> Icons.Default.LocationCity
                SyncCategory.GRADIENT -> Icons.Default.Gradient
                SyncCategory.NEON -> Icons.Default.Lightbulb
                SyncCategory.FANTASY -> Icons.Default.Castle
                SyncCategory.GAMING -> Icons.Default.SportsEsports
                SyncCategory.OCEAN -> Icons.Default.Water
                SyncCategory.MOUNTAIN -> Icons.Default.Terrain
                SyncCategory.FLOWERS -> Icons.Default.LocalFlorist
                SyncCategory.SKULL -> Icons.Default.Whatshot
                SyncCategory.TEXTURE -> Icons.Default.Texture
                SyncCategory.NEW -> Icons.Default.NewReleases
            }

@Composable
private fun WallpaperGrid(
        wallpapers: List<SyncedWallpaper>,
        newWallpapers: List<SyncedWallpaper>,
        onWallpaperClick: (SyncedWallpaper) -> Unit
) {
    LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // New wallpapers section header
        if (newWallpapers.isNotEmpty()) {
            item(span = { GridItemSpan(2) }) {
                SectionHeader(title = "✨ New Wallpapers", count = newWallpapers.size)
            }
        }

        // Wallpaper items
        items(items = wallpapers, key = { it.id }) { wallpaper ->
            SyncedWallpaperCard(
                    wallpaper = wallpaper,
                    isNew = wallpaper.isNew(),
                    onClick = { onWallpaperClick(wallpaper) }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
        )
        Badge { Text(count.toString()) }
    }
}

@Composable
private fun SyncedWallpaperCard(wallpaper: SyncedWallpaper, isNew: Boolean, onClick: () -> Unit) {
    Card(
            modifier = Modifier.fillMaxWidth().aspectRatio(0.6f).clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Wallpaper image
            AsyncImage(
                    model = wallpaper.thumbnailUrl,
                    contentDescription = wallpaper.description,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
            )

            // Gradient overlay
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .height(80.dp)
                                    .align(Alignment.BottomCenter)
                                    .background(
                                            Brush.verticalGradient(
                                                    colors =
                                                            listOf(
                                                                    Color.Transparent,
                                                                    Color.Black.copy(alpha = 0.8f)
                                                            )
                                            )
                                    )
            )

            // New badge
            if (isNew && !wallpaper.viewed) {
                Badge(
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                        containerColor = MaterialTheme.colorScheme.primary
                ) { Text("NEW") }
            }

            // Source badge
            SourceBadge(
                    source = wallpaper.source,
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
            )

            // Attribution
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)) {
                Text(
                        text = wallpaper.photographerName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                )
                Text(
                        text = wallpaper.category.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun SourceBadge(source: WallpaperSource, modifier: Modifier = Modifier) {
    val (text, color) =
            when (source) {
                WallpaperSource.UNSPLASH -> "Unsplash" to Color(0xFF111111)
                WallpaperSource.PEXELS -> "Pexels" to Color(0xFF05A081)
                WallpaperSource.WALLHAVEN -> "Wallhaven" to Color(0xFF4A148C)
                WallpaperSource.PICSUM -> "Picsum" to Color(0xFF1565C0)
                WallpaperSource.FIREBASE -> "Curated" to Color(0xFFFFA000)
                WallpaperSource.GITHUB_CDN -> "Starter" to Color(0xFF6366F1)
            }

    Surface(
            modifier = modifier,
            shape = RoundedCornerShape(4.dp),
            color = color.copy(alpha = 0.9f)
    ) {
        Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun PremiumEmptyState(onSyncClick: () -> Unit) {
    Box(
            modifier =
                    Modifier.fillMaxSize()
                            .background(
                                    Brush.radialGradient(
                                            colors =
                                                    listOf(
                                                            NeonBlue.copy(alpha = 0.1f),
                                                            Color.Transparent
                                                    ),
                                            radius = 600f
                                    )
                            ),
            contentAlignment = Alignment.Center
    ) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
        ) {
            // Icon with gradient background
            Box(
                    modifier =
                            Modifier.size(100.dp)
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(
                                            Brush.linearGradient(
                                                    colors =
                                                            listOf(
                                                                    Color(0xFF06B6D4),
                                                                    Color(0xFF3B82F6)
                                                            )
                                            )
                                    ),
                    contentAlignment = Alignment.Center
            ) {
                Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                    text = "No Wallpapers Yet",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                    text =
                            "Tap sync to fetch fresh wallpapers from Wallhaven, Picsum, Unsplash and Pexels",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = TextSecondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Premium sync button
            Box(
                    modifier =
                            Modifier.clip(RoundedCornerShape(14.dp))
                                    .background(
                                            Brush.horizontalGradient(
                                                    colors =
                                                            listOf(
                                                                    Color(0xFF06B6D4),
                                                                    Color(0xFF3B82F6)
                                                            )
                                            )
                                    )
                                    .clickable { onSyncClick() }
                                    .padding(horizontal = 28.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Sync Now", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
