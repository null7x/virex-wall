package com.virex.wallpapers.ui.theme.virex

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * VirexScreen — Base screen layout wrapper
 * 
 * Provides:
 * - Unified background color
 * - Consistent padding (horizontal + top)
 * - Bottom padding for FAB + BottomBar spacing
 * 
 * Usage:
 * VirexScreen {
 *     // Screen content
 * }
 */
@Composable
fun VirexScreen(
    modifier: Modifier = Modifier,
    applyHorizontalPadding: Boolean = true,
    applyTopPadding: Boolean = true,
    applyBottomPadding: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VirexColors.Background)
            .padding(
                start = if (applyHorizontalPadding) VirexSpacing.screenHorizontal else VirexSpacing.xxxs,
                end = if (applyHorizontalPadding) VirexSpacing.screenHorizontal else VirexSpacing.xxxs,
                top = if (applyTopPadding) VirexSpacing.screenTop else VirexSpacing.xxxs,
                bottom = if (applyBottomPadding) VirexSpacing.screenBottom else VirexSpacing.xxxs
            )
    ) {
        content()
    }
}

/**
 * VirexScreen with custom padding values
 */
@Composable
fun VirexScreen(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VirexColors.Background)
            .padding(paddingValues)
    ) {
        content()
    }
}
