package com.virex.wallpapers.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.virex.wallpapers.data.model.Wallpaper
import com.virex.wallpapers.ui.theme.AmoledBlack
import com.virex.wallpapers.ui.theme.Error
import com.virex.wallpapers.ui.theme.NeonBlue
import com.virex.wallpapers.ui.theme.OverlayDark
import com.virex.wallpapers.ui.theme.ProGold
import com.virex.wallpapers.ui.theme.TextPrimary
import com.virex.wallpapers.ui.theme.TextSecondary

/**
 * Wallpaper Grid Card
 *
 * Displays a wallpaper thumbnail with title, premium badge, and favorite button.
 */
@Composable
fun WallpaperCard(
        wallpaper: Wallpaper,
        isFavorite: Boolean,
        isPro: Boolean,
        onClick: () -> Unit,
        onFavoriteClick: () -> Unit,
        modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(targetValue = 1f, animationSpec = tween(150), label = "scale")

    Card(
            modifier = modifier.scale(scale).clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Wallpaper Image with blur for PRO content
            val shouldBlur = wallpaper.isPremium && !isPro
            AsyncImage(
                    model =
                            ImageRequest.Builder(LocalContext.current)
                                    .data(wallpaper.thumbnailUrl)
                                    .crossfade(true)
                                    .build(),
                    contentDescription = wallpaper.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                            .fillMaxSize()
                            .then(if (shouldBlur) Modifier.blur(12.dp) else Modifier)
            )

            // Gradient overlay
            Box(
                    modifier =
                            Modifier.fillMaxSize()
                                    .background(
                                            Brush.verticalGradient(
                                                    colors =
                                                            listOf(
                                                                    Color.Transparent,
                                                                    Color.Transparent,
                                                                    OverlayDark
                                                            )
                                            )
                                    )
            )

            // Premium lock icon in center
            if (wallpaper.isPremium && !isPro) {
                Box(
                        modifier = Modifier
                                .align(Alignment.Center)
                                .size(48.dp)
                                .background(
                                        color = AmoledBlack.copy(alpha = 0.6f),
                                        shape = CircleShape
                                )
                                .padding(12.dp),
                        contentAlignment = Alignment.Center
                ) {
                    Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "PRO content",
                            tint = ProGold,
                            modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Premium badge
            if (wallpaper.isPremium && !isPro) {
                Box(
                        modifier =
                                Modifier.align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(
                                                color = ProGold,
                                                shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = AmoledBlack,
                                modifier = Modifier.size(10.dp)
                        )
                        Text(
                                text = "PRO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AmoledBlack
                        )
                    }
                }
            }

            // New badge
            if (wallpaper.isNew()) {
                Box(
                        modifier =
                                Modifier.align(Alignment.TopStart)
                                        .padding(8.dp)
                                        .background(
                                                color = NeonBlue,
                                                shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                            text = "NEW",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AmoledBlack
                    )
                }
            }

            // Bottom info and favorite button
            Row(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                // Title
                Text(
                        text = wallpaper.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                )

                // Favorite button
                FavoriteButton(
                        isFavorite = isFavorite,
                        onClick = onFavoriteClick,
                        modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

/** Favorite Heart Button with spring animation */
@Composable
fun FavoriteButton(isFavorite: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val heartColor by
            animateColorAsState(
                    targetValue = if (isFavorite) Error else TextSecondary,
                    animationSpec = tween(300),
                    label = "heartColor"
            )

    // Use spring animation for bouncy effect
    val scale by
            animateFloatAsState(
                    targetValue = if (isFavorite) 1f else 1f,
                    animationSpec =
                            spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                            ),
                    label = "scale"
            )

    // Pulse effect when favorited
    val pulseScale by
            animateFloatAsState(
                    targetValue = if (isFavorite) 1.15f else 1f,
                    animationSpec =
                            spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                            ),
                    label = "pulseScale"
            )

    IconButton(onClick = onClick, modifier = modifier.scale(scale * pulseScale)) {
        Icon(
                imageVector =
                        if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription =
                        if (isFavorite) "Remove from favorites" else "Add to favorites",
                tint = heartColor
        )
    }
}

/** Category Card */
@Composable
fun CategoryCard(
        name: String,
        coverUrl: String,
        wallpaperCount: Int,
        isPremium: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
) {
    Card(
            modifier = modifier.fillMaxWidth().height(120.dp).clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Cover image
            AsyncImage(
                    model =
                            ImageRequest.Builder(LocalContext.current)
                                    .data(coverUrl)
                                    .crossfade(true)
                                    .build(),
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
            )

            // Gradient overlay
            Box(
                    modifier =
                            Modifier.fillMaxSize()
                                    .background(
                                            Brush.horizontalGradient(
                                                    colors = listOf(OverlayDark, Color.Transparent)
                                            )
                                    )
            )

            // Content
            Column(modifier = Modifier.align(Alignment.CenterStart).padding(16.dp)) {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                            text = name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                    )

                    if (isPremium) {
                        Box(
                                modifier =
                                        Modifier.background(
                                                        color = ProGold,
                                                        shape = RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                    text = "PRO",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AmoledBlack
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                        text = "$wallpaperCount wallpapers",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                )
            }
        }
    }
}

/** Section Header */
@Composable
fun SectionHeader(
        title: String,
        onSeeAllClick: (() -> Unit)? = null,
        modifier: Modifier = Modifier
) {
    Row(
            modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
        )

        if (onSeeAllClick != null) {
            Text(
                    text = "See all",
                    style = MaterialTheme.typography.labelLarge,
                    color = NeonBlue,
                    modifier = Modifier.clickable(onClick = onSeeAllClick)
            )
        }
    }
}
