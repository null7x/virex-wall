package com.virex.wallpapers.ui.premium.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.virex.wallpapers.data.model.Wallpaper
import com.virex.wallpapers.ui.premium.PremiumTheme

/**
 * Premium Wallpaper Card with press animations, haptic feedback, and glow effects
 */
@Composable
fun PremiumWallpaperCard(
    wallpaper: Wallpaper,
    isFavorite: Boolean,
    isPro: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Press animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )
    
    val shadowElevation by animateDpAsState(
        targetValue = if (isPressed) 4.dp else 12.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "shadow"
    )
    
    val shouldBlur = wallpaper.isPremium && !isPro
    
    // Shimmer animation for premium border
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                shadowElevation,
                RoundedCornerShape(PremiumTheme.CornerRadiusLarge),
                spotColor = PremiumTheme.NeonPurple.copy(alpha = 0.4f)
            )
            .clip(RoundedCornerShape(PremiumTheme.CornerRadiusLarge))
            .background(PremiumTheme.SurfaceCard)
            .then(
                if (wallpaper.isPremium && !isPro) {
                    Modifier.border(
                        2.dp,
                        Brush.linearGradient(
                            colors = listOf(
                                PremiumTheme.ProGold.copy(alpha = 0.8f),
                                PremiumTheme.ProGold.copy(alpha = 0.2f),
                                PremiumTheme.ProGold.copy(alpha = 0.8f)
                            ),
                            start = Offset(shimmerOffset - 500f, 0f),
                            end = Offset(shimmerOffset, 500f)
                        ),
                        RoundedCornerShape(PremiumTheme.CornerRadiusLarge)
                    )
                } else Modifier
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
    ) {
        // Main Image
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(wallpaper.thumbnailUrl)
                .crossfade(400)
                .build(),
            contentDescription = wallpaper.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .then(if (shouldBlur) Modifier.blur(20.dp) else Modifier),
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
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.4f),
                            Color.Black.copy(alpha = 0.85f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )
        
        // Premium lock overlay
        if (shouldBlur) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Glowing lock
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        PremiumTheme.ProGold.copy(alpha = 0.4f),
                                        Color.Transparent
                                    )
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            PremiumTheme.ProGradientStart,
                                            PremiumTheme.ProGradientEnd
                                        )
                                    ),
                                    CircleShape
                                )
                                .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "PRO",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PremiumTheme.ProGold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
        
        // NEW badge
        if (wallpaper.isNew()) {
            GlassBadge(
                text = "NEW",
                gradientColors = listOf(PremiumTheme.NeonBlue, PremiumTheme.NeonPurple),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
            )
        }
        
        // PRO badge (for unlocked premium)
        if (wallpaper.isPremium && isPro) {
            GlassBadge(
                text = "PRO",
                icon = Icons.Default.Star,
                gradientColors = listOf(PremiumTheme.ProGradientStart, PremiumTheme.ProGradientEnd),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
            )
        }
        
        // Bottom info panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = wallpaper.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = wallpaper.categoryName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                
                // Favorite button with glow
                GlowingIconButton(
                    icon = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    isActive = isFavorite,
                    activeColor = PremiumTheme.Error,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onFavoriteClick()
                    }
                )
            }
        }
    }
}

/**
 * Glass-style badge with gradient background
 */
@Composable
fun GlassBadge(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    gradientColors: List<Color> = listOf(PremiumTheme.NeonBlue, PremiumTheme.NeonPurple)
) {
    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(gradientColors),
                RoundedCornerShape(10.dp)
            )
            .border(
                1.dp,
                Color.White.copy(alpha = 0.3f),
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp
            )
        }
    }
}

/**
 * Icon button with glow effect on active state
 */
@Composable
fun GlowingIconButton(
    icon: ImageVector,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color by animateColorAsState(
        targetValue = if (isActive) activeColor else Color.White.copy(alpha = 0.8f),
        animationSpec = tween(300),
        label = "color"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .size(44.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .then(
                if (isActive) {
                    Modifier.background(
                        Brush.radialGradient(
                            colors = listOf(
                                activeColor.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
                } else Modifier
            )
            .background(
                PremiumTheme.GlassPrimary,
                CircleShape
            )
            .border(1.dp, PremiumTheme.GlassBorder, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
    }
}

/**
 * Premium shimmer loading effect
 */
@Composable
fun PremiumShimmer(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "translate"
    )
    
    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        PremiumTheme.Surface,
                        PremiumTheme.SurfaceElevated,
                        PremiumTheme.Surface
                    ),
                    start = Offset(translateAnim - 500f, translateAnim - 500f),
                    end = Offset(translateAnim, translateAnim)
                )
            )
    )
}
