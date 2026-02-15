package com.virex.wallpapers.ui.theme.virex

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * VirexEmptyState — Unified empty state component
 * 
 * Features:
 * - Centered layout
 * - Consistent icon size (80dp)
 * - Unified typography
 * - Optional action button
 */
@Composable
fun VirexEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = VirexColors.TextTertiary,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(VirexSpacing.xl))
        
        // Title
        Text(
            text = title,
            style = VirexTypography.SectionTitle,
            color = VirexColors.TextPrimary,
            textAlign = TextAlign.Center
        )
        
        // Subtitle
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(VirexSpacing.xs))
            Text(
                text = subtitle,
                style = VirexTypography.Body,
                color = VirexColors.TextSecondary,
                textAlign = TextAlign.Center
            )
        }
        
        // Action
        if (action != null) {
            Spacer(modifier = Modifier.height(VirexSpacing.xl))
            action()
        }
    }
}
