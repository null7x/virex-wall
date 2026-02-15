package com.virex.wallpapers.ui.screens.preview

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.virex.wallpapers.ads.VkAdsManager
import com.virex.wallpapers.data.model.RecommendedWallpaper
import com.virex.wallpapers.data.model.WallpaperTarget
import com.virex.wallpapers.ui.theme.AmoledBlack
import com.virex.wallpapers.ui.theme.Error
import com.virex.wallpapers.ui.theme.NeonBlue
import com.virex.wallpapers.ui.theme.NeonPurple
import com.virex.wallpapers.ui.theme.OverlayDark
import com.virex.wallpapers.ui.theme.ProGold
import com.virex.wallpapers.ui.theme.SurfaceCard
import com.virex.wallpapers.ui.theme.TextPrimary
import com.virex.wallpapers.ui.theme.TextSecondary
import kotlinx.coroutines.launch

/**
 * Wallpaper Preview Screen
 *
 * Full-screen wallpaper preview with zoom, set, and download options.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperPreviewScreen(
        wallpaperId: String,
        onBackClick: () -> Unit,
        onProClick: () -> Unit,
        onSimilarWallpaperClick: (String) -> Unit = {},
        viewModel: PreviewViewModel = hiltViewModel()
) {
    val wallpaper by viewModel.wallpaper.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isPro by viewModel.isPro.collectAsStateWithLifecycle()
    val similarWallpapers by viewModel.similarWallpapers.collectAsStateWithLifecycle()

    var showControls by remember { mutableStateOf(true) }
    var showSetWallpaperSheet by remember { mutableStateOf(false) }
    var showSimilarSheet by remember { mutableStateOf(false) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val similarSheetState = rememberModalBottomSheetState()
    val context = LocalContext.current

    LaunchedEffect(wallpaperId) { viewModel.loadWallpaper(wallpaperId) }

    // Trigger interstitial ad check when wallpaper is opened (only for non-PRO)
    LaunchedEffect(wallpaperId, isPro) {
        if (!isPro) {
            (context as? Activity)?.let { activity -> VkAdsManager.showInterstitial(activity) }
        }
    }

    // Track when user leaves the screen
    DisposableEffect(Unit) { onDispose { viewModel.onLeavePreview() } }

    LaunchedEffect(Unit) {
        viewModel.message.collect { message ->
            if (message.isNotBlank()) snackbarHostState.showSnackbar(message)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(AmoledBlack)) {
        // Wallpaper Image with zoom
        wallpaper?.let { wp ->
            AsyncImage(
                    model =
                            ImageRequest.Builder(LocalContext.current)
                                    .data(wp.fullUrl)
                                    .crossfade(true)
                                    .build(),
                    contentDescription = wp.title,
                    contentScale = ContentScale.Crop,
                    modifier =
                            Modifier.fillMaxSize()
                                    .graphicsLayer(
                                            scaleX = scale,
                                            scaleY = scale,
                                            translationX = offset.x,
                                            translationY = offset.y
                                    )
                                    .pointerInput(Unit) {
                                        detectTransformGestures { _, pan, zoom, _ ->
                                            scale = (scale * zoom).coerceIn(1f, 3f)
                                            if (scale > 1f) {
                                                offset =
                                                        Offset(
                                                                x = offset.x + pan.x,
                                                                y = offset.y + pan.y
                                                        )
                                            } else {
                                                offset = Offset.Zero
                                            }
                                        }
                                    }
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                                onTap = { showControls = !showControls },
                                                onDoubleTap = {
                                                    scale = if (scale > 1f) 1f else 2f
                                                    offset = Offset.Zero
                                                }
                                        )
                                    }
            )
        }

        // Top gradient overlay
        AnimatedVisibility(visible = showControls, enter = fadeIn(), exit = fadeOut()) {
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .height(120.dp)
                                    .background(
                                            Brush.verticalGradient(
                                                    colors = listOf(OverlayDark, Color.Transparent)
                                            )
                                    )
            )
        }

        // Bottom gradient overlay
        AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .height(200.dp)
                                    .background(
                                            Brush.verticalGradient(
                                                    colors = listOf(Color.Transparent, OverlayDark)
                                            )
                                    )
            )
        }

        // Top bar
        AnimatedVisibility(
                visible = showControls,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut()
        ) {
            Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                        onClick = onBackClick,
                        modifier =
                                Modifier.size(48.dp)
                                        .background(SurfaceCard.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                    )
                }

                Row {
                    // Favorite button
                    IconButton(
                            onClick = { viewModel.toggleFavorite() },
                            modifier =
                                    Modifier.size(48.dp)
                                            .background(SurfaceCard.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(
                                imageVector =
                                        if (isFavorite) Icons.Filled.Favorite
                                        else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Error else TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Download button
                    IconButton(
                            onClick = {
                                if (wallpaper?.isPremium == true && !isPro) {
                                    onProClick()
                                } else {
                                    viewModel.downloadWallpaper()
                                    // Show interstitial ad on download (non-PRO only)
                                    if (!isPro) {
                                        (context as? Activity)?.let { activity ->
                                            VkAdsManager.showInterstitial(activity)
                                        }
                                    }
                                }
                            },
                            modifier =
                                    Modifier.size(48.dp)
                                            .background(SurfaceCard.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download",
                                tint = TextPrimary
                        )
                    }
                }
            }
        }

        // Bottom controls
        AnimatedVisibility(
                visible = showControls,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title and info
                wallpaper?.let { wp ->
                    Text(
                            text = wp.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                                text = wp.getResolution(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                        )
                        Text(
                                text = wp.getFormattedFileSize(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                        )
                    }

                    if (wp.isPremium && !isPro) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier =
                                        Modifier.background(ProGold, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                .clickable(onClick = onProClick)
                        ) {
                            Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AmoledBlack,
                                    modifier = Modifier.size(14.dp)
                            )
                            Text(
                                    text = "PRO ONLY",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AmoledBlack
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Set Wallpaper button
                Button(
                        onClick = {
                            if (wallpaper?.isPremium == true && !isPro) {
                                onProClick()
                            } else {
                                showSetWallpaperSheet = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = NeonBlue,
                                        contentColor = AmoledBlack
                                ),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                                color = AmoledBlack,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                                imageVector = Icons.Default.Wallpaper,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                                text = "Set Wallpaper",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Similar Wallpapers button (only if we have similar wallpapers)
                if (similarWallpapers.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                            onClick = { showSimilarSheet = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors =
                                    ButtonDefaults.buttonColors(
                                            containerColor = SurfaceCard,
                                            contentColor = NeonPurple
                                    ),
                            shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                                text = "✨ Similar wallpapers (${similarWallpapers.size})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Snackbar
        SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
        ) { data ->
            Snackbar(
                    snackbarData = data,
                    containerColor = SurfaceCard,
                    contentColor = TextPrimary,
                    actionColor = NeonBlue,
                    shape = RoundedCornerShape(8.dp)
            )
        }

        // Set Wallpaper Bottom Sheet
        if (showSetWallpaperSheet) {
            ModalBottomSheet(
                    onDismissRequest = { showSetWallpaperSheet = false },
                    sheetState = sheetState,
                    containerColor = SurfaceCard,
                    dragHandle = { BottomSheetDefaults.DragHandle(color = TextSecondary) }
            ) {
                Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                            text = "Set Wallpaper",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    WallpaperOptionButton(
                            icon = Icons.Default.Home,
                            text = "Home Screen",
                            onClick = {
                                viewModel.setWallpaper(WallpaperTarget.HOME_SCREEN)
                                // Show interstitial ad on set wallpaper (non-PRO only)
                                if (!isPro) {
                                    (context as? Activity)?.let { activity ->
                                        VkAdsManager.showInterstitial(activity)
                                    }
                                }
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    showSetWallpaperSheet = false
                                }
                            }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    WallpaperOptionButton(
                            icon = Icons.Default.Lock,
                            text = "Lock Screen",
                            onClick = {
                                viewModel.setWallpaper(WallpaperTarget.LOCK_SCREEN)
                                // Show interstitial ad on set wallpaper (non-PRO only)
                                if (!isPro) {
                                    (context as? Activity)?.let { activity ->
                                        VkAdsManager.showInterstitial(activity)
                                    }
                                }
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    showSetWallpaperSheet = false
                                }
                            }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    WallpaperOptionButton(
                            icon = Icons.Default.Wallpaper,
                            text = "Both",
                            onClick = {
                                viewModel.setWallpaper(WallpaperTarget.BOTH)
                                // Show interstitial ad on set wallpaper (non-PRO only)
                                if (!isPro) {
                                    (context as? Activity)?.let { activity ->
                                        VkAdsManager.showInterstitial(activity)
                                    }
                                }
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    showSetWallpaperSheet = false
                                }
                            }
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // Similar Wallpapers Bottom Sheet
        if (showSimilarSheet && similarWallpapers.isNotEmpty()) {
            ModalBottomSheet(
                    onDismissRequest = { showSimilarSheet = false },
                    sheetState = similarSheetState,
                    containerColor = SurfaceCard,
                    dragHandle = { BottomSheetDefaults.DragHandle(color = TextSecondary) }
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                    Text(
                            text = "✨ Similar wallpapers",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                            text = "Based on your style",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp)
                    ) {
                        items(similarWallpapers) { recommended ->
                            SimilarWallpaperCard(
                                    recommended = recommended,
                                    onClick = {
                                        showSimilarSheet = false
                                        onSimilarWallpaperClick(recommended.wallpaper.id)
                                    }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun WallpaperOptionButton(icon: ImageVector, text: String, onClick: () -> Unit) {
    Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors =
                    ButtonDefaults.buttonColors(
                            containerColor = AmoledBlack,
                            contentColor = TextPrimary
                    ),
            shape = RoundedCornerShape(12.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

/** Card for similar wallpaper in the bottom sheet */
@Composable
private fun SimilarWallpaperCard(recommended: RecommendedWallpaper, onClick: () -> Unit) {
    Card(
            onClick = onClick,
            modifier = Modifier.width(120.dp).height(180.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = AmoledBlack)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                    model =
                            ImageRequest.Builder(LocalContext.current)
                                    .data(recommended.wallpaper.thumbnailUrl)
                                    .crossfade(true)
                                    .build(),
                    contentDescription = recommended.wallpaper.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
            )

            // Gradient overlay at bottom
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .height(48.dp)
                                    .align(Alignment.BottomCenter)
                                    .background(
                                            Brush.verticalGradient(
                                                    colors =
                                                            listOf(
                                                                    Color.Transparent,
                                                                    AmoledBlack.copy(alpha = 0.8f)
                                                            )
                                            )
                                    )
            )

            // Reason text
            Text(
                    text = recommended.reasonText,
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonPurple,
                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
            )
        }
    }
}
