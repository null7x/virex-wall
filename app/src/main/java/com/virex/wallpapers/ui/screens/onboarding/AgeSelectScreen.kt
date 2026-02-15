package com.virex.wallpapers.ui.screens.onboarding

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
 * Onboarding Age Select Screen
 * 
 * Premium futuristic design with glowing V logo and age selection buttons.
 */
@Composable
fun AgeSelectScreen(
        onAgeSelected: (String) -> Unit,
        onContinue: () -> Unit
) {
    var selectedAge by remember { mutableStateOf<String?>(null) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "logo_glow")
    val glowScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
            ),
            label = "glow"
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
            // Glowing V Logo
            Box(contentAlignment = Alignment.Center) {
                // Glow effect
                Box(
                        modifier = Modifier
                                .size(140.dp)
                                .scale(glowScale)
                                .blur(32.dp)
                                .background(
                                        Brush.radialGradient(
                                                colors = listOf(
                                                        GradientNeonMid.copy(alpha = 0.6f),
                                                        GradientNeonStart.copy(alpha = 0.3f),
                                                        Color.Transparent
                                                )
                                        ),
                                        RoundedCornerShape(24.dp)
                                )
                )
                
                // Logo hexagon
                Box(
                        modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                        Brush.linearGradient(
                                                colors = listOf(
                                                        GradientNeonStart,
                                                        GradientNeonMid,
                                                        GradientNeonEnd
                                                )
                                        )
                                )
                                .border(
                                        width = 2.dp,
                                        brush = Brush.linearGradient(
                                                colors = listOf(
                                                        Color.White.copy(alpha = 0.5f),
                                                        Color.White.copy(alpha = 0.1f)
                                                )
                                        ),
                                        shape = RoundedCornerShape(24.dp)
                                ),
                        contentAlignment = Alignment.Center
                ) {
                    Text(
                            text = "V",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                    text = "VIREX",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 4.sp
            )

            Text(
                    text = "WALLPAPERS",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = NeonPurple,
                    letterSpacing = 8.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                    text = "Выберите ваш возраст",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
            )

            Text(
                    text = "Это поможет нам показать подходящий контент",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Age buttons
            val ageOptions = listOf("12–17", "18–24", "25–36", "37+")
            ageOptions.forEach { age ->
                AgeButton(
                        text = age,
                        selected = selectedAge == age,
                        onClick = {
                            selectedAge = age
                            onAgeSelected(age)
                        }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Continue button
            Box(
                    modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                    if (selectedAge != null) {
                                        Brush.horizontalGradient(
                                                colors = listOf(
                                                        GradientNeonStart,
                                                        GradientNeonMid,
                                                        GradientNeonEnd
                                                )
                                        )
                                    } else {
                                        Brush.horizontalGradient(
                                                colors = listOf(
                                                        Color(0xFF2A2A2A),
                                                        Color(0xFF2A2A2A)
                                                )
                                        )
                                    }
                            )
                            .clickable(enabled = selectedAge != null) { onContinue() },
                    contentAlignment = Alignment.Center
            ) {
                Text(
                        text = "Продолжить",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedAge != null) Color.White else TextSecondary
                )
            }
        }
    }
}

@Composable
private fun AgeButton(
        text: String,
        selected: Boolean,
        onClick: () -> Unit
) {
    Box(
            modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                            if (selected) {
                                SurfaceGlass.copy(alpha = 0.4f)
                            } else {
                                SurfaceGlass
                            }
                    )
                    .border(
                            width = if (selected) 2.dp else 1.dp,
                            brush = if (selected) {
                                Brush.horizontalGradient(
                                        colors = listOf(
                                                GradientNeonStart,
                                                GradientNeonEnd
                                        )
                                )
                            } else {
                                Brush.horizontalGradient(
                                        colors = listOf(
                                                NeonPurple.copy(alpha = 0.3f),
                                                NeonPurple.copy(alpha = 0.1f)
                                        )
                                )
                            },
                            shape = RoundedCornerShape(16.dp)
                    )
                    .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
    ) {
        Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) NeonPurple else TextPrimary
        )
    }
}
