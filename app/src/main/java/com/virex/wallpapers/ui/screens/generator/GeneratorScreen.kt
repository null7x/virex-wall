package com.virex.wallpapers.ui.screens.generator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.virex.wallpapers.R
import com.virex.wallpapers.data.model.WallpaperTarget
import com.virex.wallpapers.generator.AccentColors
import com.virex.wallpapers.generator.WallpaperStyle
import com.virex.wallpapers.ui.theme.AmoledBlack
import com.virex.wallpapers.ui.theme.GradientAIEnd
import com.virex.wallpapers.ui.theme.GradientAIMid
import com.virex.wallpapers.ui.theme.GradientAIStart
import com.virex.wallpapers.ui.theme.NeonBlue
import com.virex.wallpapers.ui.theme.NeonPurple
import com.virex.wallpapers.ui.theme.ProGold
import com.virex.wallpapers.ui.theme.SurfaceCard
import com.virex.wallpapers.ui.theme.SurfaceElevated
import com.virex.wallpapers.ui.theme.SurfaceGlass
import com.virex.wallpapers.ui.theme.TextPrimary
import com.virex.wallpapers.ui.theme.TextSecondary

/**
 * AI Generator Screen - Premium Design
 *
 * Modern glassmorphism UI with AI-themed gradients for generating AMOLED wallpapers.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GeneratorScreen(onProClick: () -> Unit, viewModel: GeneratorViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isPro by viewModel.isPro.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showWallpaperMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.message.collect { message ->
            if (message.isNotBlank()) snackbarHostState.showSnackbar(message)
        }
    }

    Box(
            modifier = Modifier
                    .fillMaxSize()
                    .background(
                            Brush.verticalGradient(
                                    colors = listOf(
                                            Color(0xFF0A0015),
                                            Color(0xFF150025),
                                            Color(0xFF0A0A0A)
                                    )
                            )
                    )
    ) {
        Scaffold(
                containerColor = Color.Transparent,
                snackbarHost = {
                    SnackbarHost(snackbarHostState) { data ->
                        Snackbar(
                                snackbarData = data,
                                containerColor = SurfaceCard,
                                contentColor = TextPrimary,
                                actionColor = NeonBlue,
                                shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
        ) { innerPadding ->
            if (!isPro) {
                ProRequiredContent(
                        onProClick = onProClick,
                        modifier = Modifier.fillMaxSize().padding(innerPadding)
                )
            } else {
                Column(
                        modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .verticalScroll(rememberScrollState())
                ) {
                    // Premium AI Header
                    AIGeneratorHeader()
                    
                    // Content
                    Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Preview Area with glow effect
                        PreviewCard(uiState = uiState, modifier = Modifier.fillMaxWidth())

                        // Style Selector
                        GlassCard {
                            StyleSelector(
                                    selectedStyle = uiState.selectedStyle,
                                    onStyleSelected = viewModel::selectStyle
                            )
                        }

                        // Color Selector
                        GlassCard {
                            ColorSelector(
                                    selectedColor = uiState.accentColor,
                                    onColorSelected = viewModel::selectAccentColor
                            )
                        }

                        // Intensity Slider
                        GlassCard {
                            IntensitySlider(
                                    intensity = uiState.intensity,
                                    onIntensityChange = viewModel::setIntensity
                            )
                        }

                        // Action Buttons - Modern gradient style
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Randomize button
                            OutlinedButton(
                                    onClick = {
                                        viewModel.randomizeSeed()
                                        viewModel.generate()
                                    },
                                    modifier = Modifier.weight(1f).height(52.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPurple),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                        imageVector = Icons.Default.Casino,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                        stringResource(R.string.generator_randomize),
                                        fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Generate button with gradient
                            Box(
                                    modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                    Brush.horizontalGradient(
                                                            colors = listOf(
                                                                    GradientAIStart,
                                                                    GradientAIMid,
                                                                    GradientAIEnd
                                                            )
                                                    )
                                            )
                                            .clickable(enabled = !uiState.isGenerating) { viewModel.generate() },
                                    contentAlignment = Alignment.Center
                            ) {
                                Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                ) {
                                    if (uiState.isGenerating) {
                                        CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = Color.White,
                                                strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                            stringResource(R.string.generator_generate),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Save / Set Wallpaper buttons
                        AnimatedVisibility(
                                visible = uiState.generatedBitmap != null,
                                enter = fadeIn() + scaleIn(initialScale = 0.9f),
                                exit = fadeOut() + scaleOut(targetScale = 0.9f)
                        ) {
                            Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Save to Gallery
                                OutlinedButton(
                                        onClick = viewModel::saveToGallery,
                                        enabled = !uiState.isSaving,
                                        modifier = Modifier.weight(1f).height(52.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceElevated),
                                        shape = RoundedCornerShape(14.dp)
                                ) {
                                    if (uiState.isSaving) {
                                        CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                color = TextPrimary,
                                                strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                                imageVector = Icons.Default.Save,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.generator_save))
                                }

                                // Set as Wallpaper
                                Box(modifier = Modifier.weight(1f)) {
                                    Button(
                                            onClick = { showWallpaperMenu = true },
                                            enabled = !uiState.isSettingWallpaper,
                                            modifier = Modifier.fillMaxWidth().height(52.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                    containerColor = SurfaceCard,
                                                    contentColor = TextPrimary
                                            ),
                                            shape = RoundedCornerShape(14.dp)
                                    ) {
                                        if (uiState.isSettingWallpaper) {
                                            CircularProgressIndicator(
                                                    modifier = Modifier.size(18.dp),
                                                    color = TextPrimary,
                                                    strokeWidth = 2.dp
                                            )
                                        } else {
                                            Icon(
                                                    imageVector = Icons.Default.Wallpaper,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(stringResource(R.string.set_wallpaper))
                                    }

                                    DropdownMenu(
                                            expanded = showWallpaperMenu,
                                            onDismissRequest = { showWallpaperMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                                text = { Text(stringResource(R.string.set_home_screen)) },
                                                onClick = {
                                                    showWallpaperMenu = false
                                                    viewModel.setAsWallpaper(WallpaperTarget.HOME_SCREEN)
                                                },
                                                leadingIcon = {
                                                    Icon(Icons.Default.Home, contentDescription = null)
                                                }
                                        )
                                        DropdownMenuItem(
                                                text = { Text(stringResource(R.string.set_lock_screen)) },
                                                onClick = {
                                                    showWallpaperMenu = false
                                                    viewModel.setAsWallpaper(WallpaperTarget.LOCK_SCREEN)
                                                },
                                                leadingIcon = {
                                                    Icon(Icons.Default.Lock, contentDescription = null)
                                                }
                                        )
                                        DropdownMenuItem(
                                                text = { Text(stringResource(R.string.set_both)) },
                                                onClick = {
                                                    showWallpaperMenu = false
                                                    viewModel.setAsWallpaper(WallpaperTarget.BOTH)
                                                },
                                                leadingIcon = {
                                                    Icon(Icons.Default.PhoneAndroid, contentDescription = null)
                                                }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AIGeneratorHeader() {
    Box(
            modifier = Modifier
                    .fillMaxWidth()
                    .background(
                            Brush.verticalGradient(
                                    colors = listOf(
                                            GradientAIStart.copy(alpha = 0.3f),
                                            GradientAIMid.copy(alpha = 0.15f),
                                            Color.Transparent
                                    )
                            )
                    )
                    .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // AI Icon with gradient glow
                Box(
                        modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                        Brush.linearGradient(
                                                colors = listOf(
                                                        GradientAIStart,
                                                        GradientAIMid,
                                                        GradientAIEnd
                                                )
                                        )
                                ),
                        contentAlignment = Alignment.Center
                ) {
                    Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(14.dp))
                
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                                text = stringResource(R.string.nav_generator),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        // BETA badge
                        Box(
                                modifier = Modifier
                                        .background(
                                                Brush.horizontalGradient(
                                                        colors = listOf(
                                                                GradientAIStart.copy(alpha = 0.3f),
                                                                GradientAIEnd.copy(alpha = 0.3f)
                                                        )
                                                ),
                                                shape = RoundedCornerShape(6.dp)
                                        )
                                        .border(
                                                1.dp,
                                                GradientAIMid.copy(alpha = 0.5f),
                                                RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                    text = "BETA",
                                    color = GradientAIMid,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                            text = "Create unique AMOLED wallpapers",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassCard(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
) {
    Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                    containerColor = SurfaceGlass
            ),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = 0.08f)
            )
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun PreviewCard(uiState: GeneratorUiState, modifier: Modifier = Modifier) {
    val scale by animateFloatAsState(
            targetValue = if (uiState.isGenerating) 0.98f else 1f,
            animationSpec = tween(300),
            label = "scaleAnim"
    )
    
    Card(
            modifier = modifier.scale(scale),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D15)),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                            colors = listOf(
                                    GradientAIStart.copy(alpha = 0.3f),
                                    GradientAIMid.copy(alpha = 0.2f),
                                    GradientAIEnd.copy(alpha = 0.3f)
                            )
                    )
            )
    ) {
        Box(
                modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(9f / 16f)
                        .clip(RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
        ) {
            if (uiState.generatedBitmap != null) {
                Image(
                        bitmap = uiState.generatedBitmap.asImageBitmap(),
                        contentDescription = stringResource(R.string.generator_preview),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                )
            } else {
                // Empty state with gradient background
                Box(
                        modifier = Modifier
                                .fillMaxSize()
                                .background(
                                        Brush.radialGradient(
                                                colors = listOf(
                                                        GradientAIMid.copy(alpha = 0.15f),
                                                        Color.Transparent
                                                )
                                        )
                                ),
                        contentAlignment = Alignment.Center
                ) {
                    Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(32.dp)
                    ) {
                        Box(
                                modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(
                                                Brush.linearGradient(
                                                        colors = listOf(
                                                                GradientAIStart.copy(alpha = 0.2f),
                                                                GradientAIEnd.copy(alpha = 0.2f)
                                                        )
                                                )
                                        ),
                                contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = GradientAIMid,
                                    modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                                text = stringResource(R.string.generator_placeholder),
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Loading overlay with blur effect
            if (uiState.isGenerating) {
                Box(
                        modifier = Modifier
                                .fillMaxSize()
                                .background(AmoledBlack.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                                color = GradientAIMid,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                                text = stringResource(R.string.generator_generating),
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StyleSelector(
        selectedStyle: WallpaperStyle,
        onStyleSelected: (WallpaperStyle) -> Unit
) {
    Column {
        Text(
                text = stringResource(R.string.generator_style),
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(WallpaperStyle.entries) { style ->
                FilterChip(
                        selected = style == selectedStyle,
                        onClick = { onStyleSelected(style) },
                        label = { Text(style.displayName) },
                        colors =
                                FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonBlue.copy(alpha = 0.2f),
                                        selectedLabelColor = NeonBlue,
                                        containerColor = SurfaceCard,
                                        labelColor = TextSecondary
                                ),
                        border =
                                FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = style == selectedStyle,
                                        borderColor = SurfaceElevated,
                                        selectedBorderColor = NeonBlue
                                )
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorSelector(selectedColor: Color, onColorSelected: (Color) -> Unit) {
    Column {
        Text(
                text = stringResource(R.string.generator_accent_color),
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccentColors.presets.forEach { color ->
                val isSelected = color == selectedColor
                Box(
                        modifier =
                                Modifier.size(40.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .then(
                                                if (isSelected) {
                                                    Modifier.border(3.dp, TextPrimary, CircleShape)
                                                } else {
                                                    Modifier.border(
                                                            1.dp,
                                                            SurfaceElevated,
                                                            CircleShape
                                                    )
                                                }
                                        )
                                        .clickable { onColorSelected(color) },
                        contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint =
                                        if (color == Color.White || color == Color(0xFFFFD700)) {
                                            Color.Black
                                        } else {
                                            Color.White
                                        },
                                modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IntensitySlider(intensity: Float, onIntensityChange: (Float) -> Unit) {
    Column {
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                    text = stringResource(R.string.generator_intensity),
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
            )
            Text(
                    text = "${(intensity * 100).toInt()}%",
                    color = NeonBlue,
                    style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        Slider(
                value = intensity,
                onValueChange = onIntensityChange,
                valueRange = 0f..1f,
                colors =
                        SliderDefaults.colors(
                                thumbColor = NeonBlue,
                                activeTrackColor = NeonBlue,
                                inactiveTrackColor = SurfaceElevated
                        )
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                    text = stringResource(R.string.generator_minimal),
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelSmall
            )
            Text(
                    text = stringResource(R.string.generator_complex),
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun ProRequiredContent(onProClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
            modifier = modifier
                    .background(
                            Brush.radialGradient(
                                    colors = listOf(
                                            GradientAIMid.copy(alpha = 0.15f),
                                            Color.Transparent
                                    ),
                                    radius = 800f
                            )
                    ),
            contentAlignment = Alignment.Center
    ) {
        Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
        ) {
            // Animated AI icon
            Box(
                    modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(
                                    Brush.linearGradient(
                                            colors = listOf(
                                                    GradientAIStart,
                                                    GradientAIMid,
                                                    GradientAIEnd
                                            )
                                    )
                            ),
                    contentAlignment = Alignment.Center
            ) {
                Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                    text = stringResource(R.string.generator_pro_required_title),
                    color = TextPrimary,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                    text = stringResource(R.string.generator_pro_required_description),
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Unlock PRO button with gradient
            Box(
                    modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                    Brush.horizontalGradient(
                                            colors = listOf(
                                                    ProGold,
                                                    Color(0xFFFFA500)
                                            )
                                    )
                            )
                            .clickable { onProClick() },
                    contentAlignment = Alignment.Center
            ) {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = AmoledBlack,
                            modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                            text = stringResource(R.string.generator_unlock_pro),
                            color = AmoledBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                    )
                }
            }
        }
    }
}
