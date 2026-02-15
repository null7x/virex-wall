package com.virex.wallpapers.ui.premium.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.virex.wallpapers.ui.premium.PremiumTheme

/**
 * Navigation items
 */
enum class PremiumNavItem(
    val route: String,
    val label: String,
    val outlinedIcon: ImageVector,
    val filledIcon: ImageVector
) {
    HOME("home", "Home", Icons.Outlined.Home, Icons.Filled.Home),
    CATEGORIES("categories", "Explore", Icons.Outlined.GridView, Icons.Filled.GridView),
    FAVORITES("favorites", "Saved", Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite),
    SETTINGS("settings", "Settings", Icons.Outlined.Settings, Icons.Filled.Settings)
}

/**
 * Floating Glass Bottom Navigation
 */
@Composable
fun PremiumBottomNavigation(
    selectedItem: PremiumNavItem,
    onItemSelected: (PremiumNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Glass background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(PremiumTheme.CornerRadiusLarge),
                    spotColor = PremiumTheme.NeonBlue.copy(alpha = 0.2f)
                )
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            PremiumTheme.GlassPrimary,
                            PremiumTheme.GlassSecondary.copy(alpha = 0.8f)
                        )
                    ),
                    RoundedCornerShape(PremiumTheme.CornerRadiusLarge)
                )
                .border(
                    1.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            PremiumTheme.GlassBorder,
                            PremiumTheme.NeonBlue.copy(alpha = 0.3f),
                            PremiumTheme.GlassBorder
                        )
                    ),
                    RoundedCornerShape(PremiumTheme.CornerRadiusLarge)
                )
        )
        
        // Navigation items
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PremiumNavItem.entries.forEach { item ->
                PremiumNavButton(
                    item = item,
                    isSelected = selectedItem == item,
                    onClick = { onItemSelected(item) }
                )
            }
        }
    }
}

/**
 * Individual navigation button with glow effect
 */
@Composable
fun PremiumNavButton(
    item: PremiumNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) PremiumTheme.NeonBlue else PremiumTheme.TextSecondary,
        animationSpec = tween(200),
        label = "iconColor"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "scale"
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Glow effect for selected
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .blur(16.dp)
                        .background(
                            PremiumTheme.NeonBlue.copy(alpha = glowAlpha),
                            CircleShape
                        )
                )
            }
            
            Icon(
                imageVector = if (isSelected) item.filledIcon else item.outlinedIcon,
                contentDescription = item.label,
                tint = iconColor,
                modifier = Modifier
                    .size(26.dp)
                    .graphicsLayer { scaleX = scale; scaleY = scale }
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = PremiumTheme.NeonBlue,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * AI Morphing FAB - transforms from circle to pill
 */
@Composable
fun AiMorphingFab(
    isExpanded: Boolean,
    onClick: () -> Unit,
    onExpandChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    val fabWidth by animateDpAsState(
        targetValue = if (isExpanded) 180.dp else 56.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "fabWidth"
    )
    
    val fabHeight by animateDpAsState(
        targetValue = if (isExpanded) 56.dp else 56.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "fabHeight"
    )
    
    val cornerRadius by animateDpAsState(
        targetValue = if (isExpanded) 32.dp else 28.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "cornerRadius"
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "fabGlow")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 500f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientOffset"
    )
    
    Box(
        modifier = modifier
            .width(fabWidth)
            .height(fabHeight)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(cornerRadius),
                spotColor = PremiumTheme.NeonPurple.copy(alpha = 0.4f)
            )
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        PremiumTheme.NeonBlue,
                        PremiumTheme.NeonPurple,
                        PremiumTheme.NeonPink
                    ),
                    start = Offset(gradientOffset, 0f),
                    end = Offset(gradientOffset + 200f, 100f)
                ),
                RoundedCornerShape(cornerRadius)
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                if (isExpanded) onClick() else onExpandChange(true)
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "AI Generate",
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        rotationZ = if (isExpanded) 0f else gradientOffset / 5f % 360
                    }
            )
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "Create with AI",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "✨",
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
    
    // Auto-collapse after delay
    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            kotlinx.coroutines.delay(3000)
            onExpandChange(false)
        }
    }
}

/**
 * Mini FAB for secondary actions
 */
@Composable
fun PremiumMiniFab(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = PremiumTheme.GlassPrimary
) {
    val haptic = LocalHapticFeedback.current
    
    Box(
        modifier = modifier
            .size(48.dp)
            .shadow(8.dp, CircleShape, spotColor = PremiumTheme.NeonBlue.copy(alpha = 0.3f))
            .background(containerColor, CircleShape)
            .border(1.dp, PremiumTheme.GlassBorder, CircleShape)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}
