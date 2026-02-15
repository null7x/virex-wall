package com.virex.wallpapers.ui.screens.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.virex.wallpapers.data.model.UiState
import com.virex.wallpapers.ui.components.ErrorState
import com.virex.wallpapers.ui.components.LoadingIndicator
import com.virex.wallpapers.ui.components.WallcraftCard
import com.virex.wallpapers.ui.theme.AmoledBlack
import com.virex.wallpapers.ui.theme.TextPrimary

/**
 * Category Detail Screen
 *
 * Displays wallpapers from a specific category in a grid.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
        categoryId: String,
        onWallpaperClick: (String) -> Unit,
        onBackClick: () -> Unit,
        viewModel: CategoryDetailViewModel = hiltViewModel()
) {
    val wallpapersState by viewModel.wallpapers.collectAsStateWithLifecycle()
    val categoryName by viewModel.categoryName.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val isPro by viewModel.isPro.collectAsStateWithLifecycle()

    LaunchedEffect(categoryId) { viewModel.loadCategory(categoryId) }

    Scaffold(
            containerColor = AmoledBlack,
            topBar = {
                TopAppBar(
                        title = {
                            Text(
                                    text = categoryName.ifEmpty { "Category" },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = TextPrimary
                                )
                            }
                        },
                        colors =
                                TopAppBarDefaults.topAppBarColors(
                                        containerColor = AmoledBlack,
                                        titleContentColor = TextPrimary
                                )
                )
            }
    ) { paddingValues ->
        when (val state = wallpapersState) {
            is UiState.Loading -> {
                LoadingIndicator(modifier = Modifier.padding(paddingValues))
            }
            is UiState.Success -> {
                LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.data) { wallpaper ->
                        WallcraftCard(
                                wallpaper = wallpaper,
                                isFavorite = favorites.contains(wallpaper.id),
                                isPro = isPro,
                                onClick = { onWallpaperClick(wallpaper.id) },
                                onFavoriteClick = { viewModel.toggleFavorite(wallpaper.id) },
                                modifier = Modifier.height(280.dp)
                        )
                    }
                }
            }
            is UiState.Error -> {
                ErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadCategory(categoryId) },
                        modifier = Modifier.padding(paddingValues)
                )
            }
            is UiState.Empty -> {
                ErrorState(
                        message = "No wallpapers in this category",
                        onRetry = { viewModel.loadCategory(categoryId) },
                        modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}
