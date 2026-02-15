package com.virex.wallpapers.ui.theme.virex

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * VirexTopBar — Unified top app bar
 * 
 * Features:
 * - VIREX logo with gradient
 * - PRO/FREE badge
 * - Action buttons
 * - Consistent height and padding
 */
@Composable
fun VirexTopBar(
    modifier: Modifier = Modifier,
    showLogo: Boolean = true,
    title: String? = null,
    isPro: Boolean = false,
    onProClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(VirexDimens.topBarHeight)
            .background(VirexColors.Background)
            .padding(horizontal = VirexSpacing.screenHorizontal),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: Logo + Title + Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(VirexSpacing.xs)
        ) {
            if (showLogo) {
                // Gradient V logo
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
            }
            
            // Title
            Text(
                text = title ?: "VIREX",
                style = if (title != null) VirexTypography.ScreenTitle else VirexTypography.LargeTitle.copy(fontSize = 22.sp),
                fontWeight = FontWeight.Black,
                color = VirexColors.TextPrimary
            )
            
            // PRO/FREE badge
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
        
        // Right side: Actions
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(VirexSpacing.xs)
        ) {
            actions()
            
            // PRO upgrade button (if not PRO)
            if (!isPro && onProClick != null) {
                VirexIconButton(
                    icon = Icons.Default.Star,
                    onClick = onProClick,
                    tint = VirexColors.ProGold
                )
            }
        }
    }
}

/**
 * VirexIconButton — Unified icon button
 */
@Composable
fun VirexIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = VirexColors.TextPrimary,
    backgroundColor: Color = VirexColors.GlassPrimary
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.dp, VirexColors.GlassBorder, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(VirexDimens.topBarIconSize)
        )
    }
}
