package com.virex.wallpapers.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.virex.wallpapers.ui.theme.AmoledBlack
import com.virex.wallpapers.ui.theme.NeonBlue
import com.virex.wallpapers.ui.theme.ShimmerBase
import com.virex.wallpapers.ui.theme.ShimmerHighlight
import com.virex.wallpapers.ui.theme.TextSecondary

/** Loading indicator */
@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
                color = NeonBlue,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
        )
    }
}

/** Shimmer loading effect for grid items */
@Composable
fun ShimmerWallpaperCard(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by
            transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1000f,
                    animationSpec =
                            infiniteRepeatable(
                                    animation = tween(durationMillis = 1200),
                                    repeatMode = RepeatMode.Restart
                            ),
                    label = "shimmer"
            )

    val shimmerBrush =
            Brush.linearGradient(
                    colors = listOf(ShimmerBase, ShimmerHighlight, ShimmerBase),
                    start = Offset(translateAnim - 500f, translateAnim - 500f),
                    end = Offset(translateAnim, translateAnim)
            )

    Box(
            modifier =
                    modifier.aspectRatio(9f / 16f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(shimmerBrush)
    )
}

/** Error state with retry button */
@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
            modifier = modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
    ) {
        Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
                onClick = onRetry,
                colors =
                        ButtonDefaults.buttonColors(
                                containerColor = NeonBlue,
                                contentColor = AmoledBlack
                        )
        ) {
            Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text("Retry")
        }
    }
}

/** Empty state with icon and message */
@Composable
fun EmptyState(
        icon: ImageVector,
        title: String,
        subtitle: String? = null,
        modifier: Modifier = Modifier
) {
    Column(
            modifier = modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
    ) {
        Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center
        )

        if (subtitle != null) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
            )
        }
    }
}

/** No favorites state */
@Composable
fun NoFavoritesState(modifier: Modifier = Modifier) {
    EmptyState(
            icon = Icons.Outlined.FavoriteBorder,
            title = "No favorites yet",
            subtitle = "Tap the heart icon on any wallpaper to add it to your favorites",
            modifier = modifier
    )
}

/** No internet state */
@Composable
fun NoInternetState(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
            modifier = modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
    ) {
        Icon(
                imageVector = Icons.Outlined.CloudOff,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
                text = "No internet connection",
                style = MaterialTheme.typography.titleLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
                text = "Check your connection and try again",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
                onClick = onRetry,
                colors =
                        ButtonDefaults.buttonColors(
                                containerColor = NeonBlue,
                                contentColor = AmoledBlack
                        )
        ) { Text("Try Again") }
    }
}
