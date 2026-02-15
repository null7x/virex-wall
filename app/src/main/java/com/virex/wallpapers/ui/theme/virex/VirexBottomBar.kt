package com.virex.wallpapers.ui.theme.virex

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============================================================
// PREMIUM COLOR PALETTE
// ============================================================

private object BottomBarColors {
    // Bar background gradient
    val BarGradientStart = Color(0xFF0E1016)
    val BarGradientEnd = Color(0xFF090B10)
    
    // Accent colors
    val AccentCyan = Color(0xFF6FE3FF)
    val AccentPurple = Color(0xFFB388FF)
    
    // Inactive state
    val InactiveWhite = Color.White.copy(alpha = 0.45f)
    
    // Active capsule background
    val ActiveCapsuleBg = Color(0xFF6FE3FF).copy(alpha = 0.15f)
    
    // Border/stroke
    val StrokeBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.12f),
            Color.White.copy(alpha = 0.06f),
            Color.White.copy(alpha = 0.12f)
        )
    )
    
    // FAB gradient
    val FabGradient = Brush.radialGradient(
        colors = listOf(
            Color(0xFFB388FF),
            Color(0xFF6FE3FF)
        )
    )
    
    // FAB glow
    val FabGlow = Color(0xFFB388FF).copy(alpha = 0.30f)
}

// ============================================================
// DIMENSIONS
// ============================================================

private object BottomBarDimens {
    val BarHeight = 74.dp
    val BarHorizontalPadding = 18.dp
    val BarBottomPadding = 14.dp
    val BarCornerRadius = 30.dp
    
    val FabSize = 66.dp
    val FabIconSize = 28.dp
    val FabOffset = 20.dp
    val FabGlowBlur = 40.dp
    
    val NavIconSize = 24.dp
    val NavLabelSize = 10.sp
    
    val CapsuleHorizontalPadding = 14.dp
    val CapsuleVerticalPadding = 6.dp
    val CapsuleCornerRadius = 16.dp
}

/**
 * Navigation item definition
 */
enum class VirexNavItem(
    val route: String,
    val label: String,
    val outlinedIcon: ImageVector,
    val filledIcon: ImageVector
) {
    EXPLORE("home", "Explore", Icons.Outlined.Explore, Icons.Filled.Explore),
    COLLECTIONS("categories", "Collections", Icons.Outlined.Collections, Icons.Filled.Collections),
    SYNC("auto_sync", "Sync", Icons.Outlined.Sync, Icons.Filled.Sync),
    FAVORITES("favorites", "Favorites", Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite)
}

// ============================================================
// VIREX BOTTOM BAR — ULTRA PREMIUM FLOATING NAVIGATION
// Style level: Wallcraft / Spotify / iOS 18
// ============================================================

/**
 * VirexBottomBar — Ultra premium floating bottom navigation
 * 
 * Features:
 * - Floating glass bar with soft blur
 * - Center FAB cutout with glow
 * - Animated capsule selection
 * - Press scale animations
 * - Perfectly symmetric layout
 */
@Composable
fun VirexBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onFabClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // FAB pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse")
    val fabPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fabPulse"
    )
    
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Main floating bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = BottomBarDimens.BarHorizontalPadding,
                    end = BottomBarDimens.BarHorizontalPadding,
                    bottom = BottomBarDimens.BarBottomPadding
                )
                // Shadow
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(BottomBarDimens.BarCornerRadius),
                    spotColor = Color.Black.copy(alpha = 0.5f)
                )
                // Background
                .clip(RoundedCornerShape(BottomBarDimens.BarCornerRadius))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            BottomBarColors.BarGradientStart,
                            BottomBarColors.BarGradientEnd
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
                // Gradient stroke border
                .border(
                    width = 1.dp,
                    brush = BottomBarColors.StrokeBrush,
                    shape = RoundedCornerShape(BottomBarDimens.BarCornerRadius)
                )
                .height(BottomBarDimens.BarHeight)
        ) {
            // Navigation items row
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Explore + Collections
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PremiumNavItem(
                        item = VirexNavItem.EXPLORE,
                        isSelected = currentRoute == VirexNavItem.EXPLORE.route,
                        onClick = { onNavigate(VirexNavItem.EXPLORE.route) }
                    )
                    PremiumNavItem(
                        item = VirexNavItem.COLLECTIONS,
                        isSelected = currentRoute == VirexNavItem.COLLECTIONS.route,
                        onClick = { onNavigate(VirexNavItem.COLLECTIONS.route) }
                    )
                }
                
                // Center FAB spacer (exactly matches FAB size)
                Spacer(modifier = Modifier.width(BottomBarDimens.FabSize))
                
                // Right side: Sync + Favorites
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PremiumNavItem(
                        item = VirexNavItem.SYNC,
                        isSelected = currentRoute == VirexNavItem.SYNC.route,
                        onClick = { onNavigate(VirexNavItem.SYNC.route) }
                    )
                    PremiumNavItem(
                        item = VirexNavItem.FAVORITES,
                        isSelected = currentRoute == VirexNavItem.FAVORITES.route,
                        onClick = { onNavigate(VirexNavItem.FAVORITES.route) }
                    )
                }
            }
        }
        
        // Center FAB with glow
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-BottomBarDimens.BarHeight / 2 - BottomBarDimens.FabOffset))
        ) {
            // FAB button
            PremiumFab(
                onClick = onFabClick,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

/**
 * PremiumNavItem — Navigation item with animated capsule selection
 */
@Composable
private fun PremiumNavItem(
    item: VirexNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Animated colors
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) BottomBarColors.AccentCyan else BottomBarColors.InactiveWhite,
        animationSpec = tween(200),
        label = "iconColor"
    )
    
    val labelColor by animateColorAsState(
        targetValue = if (isSelected) BottomBarColors.AccentCyan else BottomBarColors.InactiveWhite,
        animationSpec = tween(200),
        label = "labelColor"
    )
    
    // Animated scale for press
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(),
        label = "scale"
    )
    
    // Animated capsule padding
    val capsuleHorizontalPadding by animateDpAsState(
        targetValue = if (isSelected) BottomBarDimens.CapsuleHorizontalPadding else 8.dp,
        animationSpec = tween(200),
        label = "capsulePadding"
    )
    
    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(BottomBarDimens.CapsuleCornerRadius))
            .then(
                if (isSelected) {
                    Modifier.background(
                        BottomBarColors.ActiveCapsuleBg,
                        RoundedCornerShape(BottomBarDimens.CapsuleCornerRadius)
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
            .padding(
                horizontal = capsuleHorizontalPadding,
                vertical = BottomBarDimens.CapsuleVerticalPadding
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (isSelected) item.filledIcon else item.outlinedIcon,
                contentDescription = item.label,
                tint = iconColor,
                modifier = Modifier.size(BottomBarDimens.NavIconSize)
            )
            
            // Always reserve space for label (prevents jumping)
            Text(
                text = item.label,
                fontSize = BottomBarDimens.NavLabelSize,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = labelColor,
                maxLines = 1
            )
        }
    }
}

/**
 * PremiumFab — Center floating action button with glow and press animation
 */
@Composable
private fun PremiumFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Press scale animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(),
        label = "fabScale"
    )
    
    Box(
        modifier = modifier
            .size(BottomBarDimens.FabSize)
            .scale(scale)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                spotColor = BottomBarColors.AccentPurple
            )
            .clip(CircleShape)
            .background(BottomBarColors.FabGradient)
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.4f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = "AI Generator",
            tint = Color.White,
            modifier = Modifier.size(BottomBarDimens.FabIconSize)
        )
    }
}

/**
 * VirexFab — Public FAB component (for external use if needed)
 */
@Composable
fun VirexFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PremiumFab(onClick = onClick, modifier = modifier)
}
