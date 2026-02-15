package com.virex.wallpapers.ui.screens.pro

import android.app.Activity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.virex.wallpapers.ui.theme.AmoledBlack
import com.virex.wallpapers.ui.theme.NeonBlue
import com.virex.wallpapers.ui.theme.ProGold
import com.virex.wallpapers.ui.theme.ProGradientEnd
import com.virex.wallpapers.ui.theme.ProGradientStart
import com.virex.wallpapers.ui.theme.Success
import com.virex.wallpapers.ui.theme.SurfaceCard
import com.virex.wallpapers.ui.theme.TextPrimary
import com.virex.wallpapers.ui.theme.TextSecondary

/**
 * PRO Upgrade Screen - Premium Gold Design
 *
 * Luxurious UI with gold gradients for showcasing PRO features.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProUpgradeScreen(onBackClick: () -> Unit, viewModel: ProViewModel = hiltViewModel()) {
    val isPro by viewModel.isPro.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val price by viewModel.price.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.message.collect { message ->
            if (message.isNotBlank()) snackbarHostState.showSnackbar(message)
        }
    }

    Box(
            modifier = Modifier
                    .fillMaxSize()
                    .background(AmoledBlack)
    ) {
        // Gold gradient overlay at top
        Box(
                modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(
                                Brush.verticalGradient(
                                        colors = listOf(
                                                ProGold.copy(alpha = 0.15f),
                                                ProGradientEnd.copy(alpha = 0.08f),
                                                Color.Transparent
                                        )
                                )
                        )
        )

        Scaffold(
                containerColor = Color.Transparent,
                snackbarHost = {
                    SnackbarHost(snackbarHostState) { data ->
                        Snackbar(
                                snackbarData = data,
                                containerColor = SurfaceCard,
                                contentColor = TextPrimary,
                                actionColor = ProGold,
                                shape = RoundedCornerShape(12.dp)
                        )
                    }
                },
                topBar = {
                    TopAppBar(
                            title = {},
                            navigationIcon = {
                                IconButton(onClick = onBackClick) {
                                    Box(
                                            modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(SurfaceCard.copy(alpha = 0.5f)),
                                            contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Back",
                                                tint = TextPrimary
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
        ) { paddingValues ->
            Column(
                    modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Premium Crown Badge
                Box(
                        modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(
                                        Brush.linearGradient(
                                                colors = listOf(
                                                        ProGradientStart,
                                                        ProGold,
                                                        ProGradientEnd
                                                )
                                        )
                                )
                                .border(
                                        2.dp,
                                        Brush.linearGradient(
                                                colors = listOf(
                                                        Color.White.copy(alpha = 0.5f),
                                                        ProGold.copy(alpha = 0.3f)
                                                )
                                        ),
                                        RoundedCornerShape(28.dp)
                                ),
                        contentAlignment = Alignment.Center
                ) {
                    Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AmoledBlack,
                            modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                        text = "VIREX PRO",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = ProGold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                        text = "Unlock the full experience",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Features list with premium cards
                ProFeatureCard(
                        icon = Icons.Default.Wallpaper,
                        title = "Unlimited Wallpapers",
                        description = "Access all wallpapers including exclusive collections",
                        gradientColors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                ProFeatureCard(
                        icon = Icons.Default.AutoAwesome,
                        title = "AI Wallpaper Generator",
                        description = "Create unlimited custom AMOLED wallpapers",
                        gradientColors = listOf(Color(0xFFEC4899), Color(0xFFF97316))
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                ProFeatureCard(
                        icon = Icons.Default.CloudSync,
                        title = "Auto Sync",
                        description = "Automatic wallpaper sync and updates",
                        gradientColors = listOf(Color(0xFF06B6D4), Color(0xFF3B82F6))
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                ProFeatureCard(
                        icon = Icons.Default.Timer,
                        title = "Auto Wallpaper Change",
                        description = "Automatically change wallpaper on schedule",
                        gradientColors = listOf(Color(0xFF10B981), Color(0xFF14B8A6))
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                ProFeatureCard(
                        icon = Icons.Default.Wallpaper,
                        title = "No Ads",
                        description = "Enjoy completely ad-free experience",
                        gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFEF4444))
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Price and purchase section
                if (isPro) {
                    // Already PRO badge
                    Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                    containerColor = Success.copy(alpha = 0.15f)
                            ),
                            border = BorderStroke(1.dp, Success.copy(alpha = 0.3f))
                    ) {
                        Row(
                                modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = Success,
                                    modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                    text = "You have PRO!",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Success
                            )
                        }
                    }
                } else {
                    // Price display
                    Text(
                            text = price,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                    )

                    Text(
                            text = "One-time purchase",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Purchase button with gold gradient
                    Box(
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                            Brush.horizontalGradient(
                                                    colors = listOf(
                                                            ProGradientStart,
                                                            ProGold,
                                                            ProGradientEnd
                                                    )
                                            )
                                    )
                                    .clickable(enabled = !isLoading) {
                                        val activity = context as? Activity
                                        if (activity != null) {
                                            viewModel.purchase(activity)
                                        }
                                    },
                            contentAlignment = Alignment.Center
                    ) {
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = AmoledBlack,
                                        strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = AmoledBlack,
                                        modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                    text = if (isLoading) "Loading..." else "Upgrade to PRO",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AmoledBlack
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                        text = "Secure payment via RuStore",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun ProFeatureCard(
        icon: ImageVector,
        title: String,
        description: String,
        gradientColors: List<Color>
) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with gradient background
            Box(
                    modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                    Brush.linearGradient(colors = gradientColors)
                            ),
                    contentAlignment = Alignment.Center
            ) {
                Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                )
            }

            Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = ProGold,
                    modifier = Modifier.size(22.dp)
            )
        }
    }
}
