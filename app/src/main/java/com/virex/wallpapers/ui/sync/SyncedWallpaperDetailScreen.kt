package com.virex.wallpapers.ui.sync

import android.app.Activity
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.virex.wallpapers.ads.VkAdsManager
import com.virex.wallpapers.billing.ProStatus
import com.virex.wallpapers.data.model.SyncedWallpaper
import com.virex.wallpapers.data.model.WallpaperSource
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Detail screen for synced wallpaper Shows full image with download and set wallpaper options */
@Composable
fun SyncedWallpaperDetailScreen(
        wallpaperId: String,
        onNavigateBack: () -> Unit,
        viewModel: SyncViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var wallpaper by remember { mutableStateOf<SyncedWallpaper?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSettingWallpaper by remember { mutableStateOf(false) }
    val isPro = ProStatus.isPro(context)

    val scaffoldState =
            rememberBottomSheetScaffoldState(
                    bottomSheetState =
                            rememberStandardBottomSheetState(
                                    initialValue = SheetValue.PartiallyExpanded,
                                    skipHiddenState = true
                            )
            )

    // Load wallpaper details
    LaunchedEffect(wallpaperId) {
        wallpaper = viewModel.getWallpaper(wallpaperId)
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentWallpaper = wallpaper
    if (currentWallpaper == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Wallpaper not found")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onNavigateBack) { Text("Go Back") }
            }
        }
        return
    }

    BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 200.dp,
            sheetContent = {
                WallpaperInfoSheet(
                        wallpaper = currentWallpaper,
                        isSettingWallpaper = isSettingWallpaper,
                        onSetHomeScreen = {
                            scope.launch {
                                isSettingWallpaper = true
                                setWallpaper(context, currentWallpaper, WallpaperTarget.HOME)
                                viewModel.trackDownload(currentWallpaper)
                                isSettingWallpaper = false
                                // Show ad for non-PRO users
                                if (!isPro) {
                                    (context as? Activity)?.let {
                                        VkAdsManager.showInterstitial(it)
                                    }
                                }
                            }
                        },
                        onSetLockScreen = {
                            scope.launch {
                                isSettingWallpaper = true
                                setWallpaper(context, currentWallpaper, WallpaperTarget.LOCK)
                                viewModel.trackDownload(currentWallpaper)
                                isSettingWallpaper = false
                                // Show ad for non-PRO users
                                if (!isPro) {
                                    (context as? Activity)?.let {
                                        VkAdsManager.showInterstitial(it)
                                    }
                                }
                            }
                        },
                        onSetBoth = {
                            scope.launch {
                                isSettingWallpaper = true
                                setWallpaper(context, currentWallpaper, WallpaperTarget.BOTH)
                                viewModel.trackDownload(currentWallpaper)
                                isSettingWallpaper = false
                                // Show ad for non-PRO users
                                if (!isPro) {
                                    (context as? Activity)?.let {
                                        VkAdsManager.showInterstitial(it)
                                    }
                                }
                            }
                        },
                        onOpenSource = {
                            val intent =
                                    Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(currentWallpaper.sourceUrl)
                                    )
                            context.startActivity(intent)
                        }
                )
            },
            containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Full screen wallpaper preview
            AsyncImage(
                    model =
                            ImageRequest.Builder(context)
                                    .data(currentWallpaper.fullUrl)
                                    .crossfade(true)
                                    .build(),
                    contentDescription = currentWallpaper.description,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
            )

            // Top gradient for back button visibility
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .height(100.dp)
                                    .background(
                                            Brush.verticalGradient(
                                                    colors =
                                                            listOf(
                                                                    Color.Black.copy(alpha = 0.6f),
                                                                    Color.Transparent
                                                            )
                                            )
                                    )
            )

            // Back button
            IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
            ) {
                Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                )
            }

            // Source badge
            SourceBadgeDetail(
                    source = currentWallpaper.source,
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            )
        }
    }
}

@Composable
private fun WallpaperInfoSheet(
        wallpaper: SyncedWallpaper,
        isSettingWallpaper: Boolean,
        onSetHomeScreen: () -> Unit,
        onSetLockScreen: () -> Unit,
        onSetBoth: () -> Unit,
        onOpenSource: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp).animateContentSize()) {
        // Handle bar
        Box(
                modifier =
                        Modifier.width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.4f
                                        )
                                )
                                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Photographer attribution
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = wallpaper.getAttribution(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                )
                if (wallpaper.description != null) {
                    Text(
                            text = wallpaper.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Open source link
            IconButton(onClick = onOpenSource) {
                Icon(imageVector = Icons.Default.Link, contentDescription = "Open source")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Resolution info
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InfoChip(icon = Icons.Default.Info, text = "${wallpaper.width} × ${wallpaper.height}")
            InfoChip(icon = Icons.Default.Download, text = wallpaper.category.displayName)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons
        if (isSettingWallpaper) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(onClick = onSetHomeScreen, modifier = Modifier.weight(1f)) {
                    Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Home")
                }

                FilledTonalButton(onClick = onSetLockScreen, modifier = Modifier.weight(1f)) {
                    Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Lock")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                    onClick = onSetBoth,
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                            ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                            )
            ) {
                Icon(imageVector = Icons.Default.Smartphone, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Set Both Screens")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Attribution notice
        Text(
                text =
                        when (wallpaper.source) {
                            WallpaperSource.UNSPLASH -> "Free to use under Unsplash License"
                            WallpaperSource.PEXELS -> "Free for personal and commercial use"
                            WallpaperSource.WALLHAVEN -> "Free to use under Wallhaven terms"
                            WallpaperSource.PICSUM -> "Free to use via Lorem Picsum"
                            WallpaperSource.FIREBASE -> "Curated by VIREX team"
                            WallpaperSource.GITHUB_CDN -> "VIREX Starter Collection"
                        },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                    text = text,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SourceBadgeDetail(source: WallpaperSource, modifier: Modifier = Modifier) {
    val (text, color) =
            when (source) {
                WallpaperSource.UNSPLASH -> "Unsplash" to Color(0xFF111111)
                WallpaperSource.PEXELS -> "Pexels" to Color(0xFF05A081)
                WallpaperSource.WALLHAVEN -> "Wallhaven" to Color(0xFF4A148C)
                WallpaperSource.PICSUM -> "Picsum" to Color(0xFF1565C0)
                WallpaperSource.FIREBASE -> "Curated" to Color(0xFFFFA000)
                WallpaperSource.GITHUB_CDN -> "Starter" to Color(0xFF6366F1)
            }

    Surface(modifier = modifier, shape = RoundedCornerShape(8.dp), color = color) {
        Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/** Wallpaper target for setting */
private enum class WallpaperTarget {
    HOME,
    LOCK,
    BOTH
}

/** Set wallpaper from URL */
private suspend fun setWallpaper(
        context: Context,
        wallpaper: SyncedWallpaper,
        target: WallpaperTarget
) {
    withContext(Dispatchers.IO) {
        try {
            val wallpaperManager = WallpaperManager.getInstance(context)

            // Download image
            val url = URL(wallpaper.fullUrl)
            val connection = url.openConnection()
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            val inputStream = connection.getInputStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap != null) {
                when (target) {
                    WallpaperTarget.HOME -> {
                        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                    }
                    WallpaperTarget.LOCK -> {
                        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                    }
                    WallpaperTarget.BOTH -> {
                        wallpaperManager.setBitmap(bitmap)
                    }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Wallpaper set successfully!", Toast.LENGTH_SHORT)
                            .show()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
