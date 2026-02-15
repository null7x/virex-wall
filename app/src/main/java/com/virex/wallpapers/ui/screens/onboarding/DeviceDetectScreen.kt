package com.virex.wallpapers.ui.screens.onboarding

import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.virex.wallpapers.ui.theme.BackgroundGradientEnd
import com.virex.wallpapers.ui.theme.BackgroundGradientMid
import com.virex.wallpapers.ui.theme.BackgroundGradientStart
import com.virex.wallpapers.ui.theme.GradientNeonEnd
import com.virex.wallpapers.ui.theme.GradientNeonMid
import com.virex.wallpapers.ui.theme.GradientNeonStart
import com.virex.wallpapers.ui.theme.NeonPurple
import com.virex.wallpapers.ui.theme.SurfaceGlass
import com.virex.wallpapers.ui.theme.TextPrimary
import com.virex.wallpapers.ui.theme.TextSecondary

/**
 * Device Detection Screen
 * 
 * Shows device model and resolution in a premium glassmorphism card design.
 */
@Composable
fun DeviceDetectScreen(
        onContinue: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.roundToPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.roundToPx() }

    val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"
    val resolution = "${screenWidthPx}x${screenHeightPx}"
    val androidVersion = "Android ${Build.VERSION.RELEASE}"

    val infiniteTransition = rememberInfiniteTransition(label = "card_glow")
    val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.6f,
            animationSpec = infiniteRepeatable(
                    animation = tween(2500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
            ),
            label = "glow_alpha"
    )

    Box(
            modifier = Modifier
                    .fillMaxSize()
                    .background(
                            Brush.verticalGradient(
                                    colors = listOf(
                                            BackgroundGradientStart,
                                            BackgroundGradientMid,
                                            BackgroundGradientEnd
                                    )
                            )
                    )
    ) {
        Column(
                modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
        ) {
            // Header
            Text(
                    text = "Ваше устройство",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                    text = "Мы подберём идеальные обои",
                    fontSize = 14.sp,
                    color = TextSecondary
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Glassmorphism card
            Box(contentAlignment = Alignment.Center) {
                // Glow effect behind card
                Box(
                        modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                                .scale(1.05f)
                                .blur(40.dp)
                                .background(
                                        Brush.radialGradient(
                                                colors = listOf(
                                                        GradientNeonMid.copy(alpha = glowAlpha),
                                                        GradientNeonStart.copy(alpha = glowAlpha * 0.5f),
                                                        Color.Transparent
                                                )
                                        ),
                                        RoundedCornerShape(32.dp)
                                )
                )

                // Glass card
                Column(
                        modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(28.dp))
                                .background(SurfaceGlass)
                                .border(
                                        width = 1.dp,
                                        brush = Brush.linearGradient(
                                                colors = listOf(
                                                        Color.White.copy(alpha = 0.2f),
                                                        Color.White.copy(alpha = 0.05f),
                                                        NeonPurple.copy(alpha = 0.2f)
                                                )
                                        ),
                                        shape = RoundedCornerShape(28.dp)
                                )
                                .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    DeviceInfoRow(
                            icon = Icons.Outlined.Smartphone,
                            label = "Модель",
                            value = deviceModel
                    )

                    DeviceInfoRow(
                            icon = Icons.Outlined.AspectRatio,
                            label = "Разрешение",
                            value = resolution
                    )

                    DeviceInfoRow(
                            icon = Icons.Outlined.Memory,
                            label = "Система",
                            value = androidVersion
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Continue button with gradient
            Box(
                    modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                    Brush.horizontalGradient(
                                            colors = listOf(
                                                    GradientNeonStart,
                                                    GradientNeonMid,
                                                    GradientNeonEnd
                                            )
                                    )
                            )
                            .clickable { onContinue() },
                    contentAlignment = Alignment.Center
            ) {
                Text(
                        text = "Начать",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                )
            }
        }
    }
}

@Composable
private fun DeviceInfoRow(
        icon: ImageVector,
        label: String,
        value: String
) {
    Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
                modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                                Brush.linearGradient(
                                        colors = listOf(
                                                GradientNeonStart.copy(alpha = 0.3f),
                                                GradientNeonEnd.copy(alpha = 0.2f)
                                        )
                                )
                        ),
                contentAlignment = Alignment.Center
        ) {
            Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = NeonPurple,
                    modifier = Modifier.size(24.dp)
            )
        }

        Column {
            Text(
                    text = label,
                    fontSize = 12.sp,
                    color = TextSecondary
            )
            Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
            )
        }
    }
}
