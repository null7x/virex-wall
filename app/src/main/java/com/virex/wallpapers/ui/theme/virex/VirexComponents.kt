package com.virex.wallpapers.ui.theme.virex

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.virex.wallpapers.data.model.Wallpaper

/**
 * VirexCard — Unified card component
 * 
 * Features:
 * - Consistent corner radius (24dp)
 * - Press animation with scale
 * - Haptic feedback
 * - Premium blur overlay
 * - Glass-style favorite button
 */
@Composable
fun VirexCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) VirexAnimations.scalePressed else VirexAnimations.scaleNormal,
        animationSpec = spring(stiffness = 400f),
        label = "cardScale"
    )
    
    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        shape = RoundedCornerShape(VirexShapes.card),
        color = VirexColors.SurfaceElevated
    ) {
        content()
    }
}

/**
 * VirexWallpaperCard — Wallpaper card with image, title, and actions
 */
@Composable
fun VirexWallpaperCard(
    wallpaper: Wallpaper,
    isFavorite: Boolean,
    isPro: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = VirexDimens.wallpaperCardHeight
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) VirexAnimations.scalePressed else VirexAnimations.scaleNormal,
        animationSpec = spring(stiffness = 400f),
        label = "cardScale"
    )
    
    Box(
        modifier = modifier
            .height(height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(VirexShapes.card),
                spotColor = VirexColors.Primary.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(VirexShapes.card))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
    ) {
        // Image
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(wallpaper.thumbnailUrl)
                .crossfade(VirexAnimations.durationMedium)
                .build(),
            contentDescription = wallpaper.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = {
                VirexShimmer(modifier = Modifier.fillMaxSize())
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
                            Color.Black.copy(alpha = 0.7f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )
        
        // Premium blur overlay
        if (wallpaper.isPremium && !isPro) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(16.dp)
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "PRO",
                    tint = VirexColors.ProGold,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        
        // NEW badge
        if (wallpaper.isNew()) {
            VirexBadge(
                text = "NEW",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(VirexSpacing.sm)
            )
        }
        
        // PRO badge
        if (wallpaper.isPremium && !isPro) {
            VirexBadge(
                text = "PRO",
                color = VirexColors.ProGold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(VirexSpacing.sm)
            )
        }
        
        // Bottom content
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(VirexSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title and category
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wallpaper.title,
                    style = VirexTypography.CardTitle,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = wallpaper.categoryName,
                    style = VirexTypography.Caption,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            
            // Favorite button
            VirexFavoriteButton(
                isFavorite = isFavorite,
                onClick = onFavoriteClick
            )
        }
    }
}

/**
 * VirexFavoriteButton — Glass-style favorite button
 */
@Composable
fun VirexFavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(VirexColors.GlassPrimary)
            .border(1.dp, VirexColors.GlassBorder, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
            tint = if (isFavorite) Color.Red else Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * VirexBadge — Badge component for NEW, PRO, etc.
 */
@Composable
fun VirexBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = VirexColors.Primary
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(VirexShapes.radiusSm),
        color = color
    ) {
        Text(
            text = text,
            style = VirexTypography.Label,
            color = Color.White,
            modifier = Modifier.padding(horizontal = VirexSpacing.xs, vertical = VirexSpacing.xxs)
        )
    }
}

/**
 * VirexShimmer — Loading placeholder
 */
@Composable
fun VirexShimmer(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        VirexColors.Surface,
                        VirexColors.SurfaceElevated,
                        VirexColors.Surface
                    )
                )
            )
    )
}

/**
 * VirexSettingsItem — Settings list item
 */
@Composable
fun VirexSettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(VirexShapes.card))
            .background(VirexColors.SurfaceElevated)
            .clickable(onClick = onClick)
            .padding(VirexSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(VirexColors.GlassPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = VirexColors.Primary,
                modifier = Modifier.size(VirexDimens.iconMd)
            )
        }
        
        Spacer(modifier = Modifier.width(VirexSpacing.md))
        
        // Text
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = VirexTypography.CardTitle,
                color = VirexColors.TextPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = VirexTypography.Caption,
                    color = VirexColors.TextSecondary
                )
            }
        }
        
        // Trailing
        trailing?.invoke()
    }
}
