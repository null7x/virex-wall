package com.virex.wallpapers.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * VIREX Dark Color Scheme
 *
 * Pure AMOLED black design with neon blue accents. Optimized for battery efficiency on OLED
 * displays.
 */
private val VirexDarkColorScheme =
        darkColorScheme(
                // Primary colors
                primary = NeonBlue,
                onPrimary = AmoledBlack,
                primaryContainer = NeonBlueDark,
                onPrimaryContainer = TextPrimary,

                // Secondary colors
                secondary = NeonBlueLight,
                onSecondary = AmoledBlack,
                secondaryContainer = SurfaceCard,
                onSecondaryContainer = TextPrimary,

                // Tertiary colors
                tertiary = ProGold,
                onTertiary = AmoledBlack,
                tertiaryContainer = SurfaceElevated,
                onTertiaryContainer = ProGold,

                // Background colors
                background = AmoledBlack,
                onBackground = TextPrimary,

                // Surface colors
                surface = AmoledBlack,
                onSurface = TextPrimary,
                surfaceVariant = SurfaceCard,
                onSurfaceVariant = TextSecondary,
                surfaceTint = NeonBlue,

                // Container colors
                surfaceContainerLowest = AmoledBlack,
                surfaceContainerLow = SurfaceDark,
                surfaceContainer = SurfaceElevated,
                surfaceContainerHigh = SurfaceCard,
                surfaceContainerHighest = SurfaceHighlight,

                // Inverse colors
                inverseSurface = TextPrimary,
                inverseOnSurface = AmoledBlack,
                inversePrimary = NeonBlueDark,

                // Error colors
                error = Error,
                onError = AmoledBlack,
                errorContainer = Error.copy(alpha = 0.2f),
                onErrorContainer = Error,

                // Outline colors
                outline = TextTertiary,
                outlineVariant = SurfaceCard,

                // Scrim
                scrim = OverlayDark
        )

/**
 * VIREX Theme
 *
 * Dark-only theme for AMOLED wallpaper app. Always uses dark theme regardless of system settings.
 */
@Composable
fun VirexTheme(content: @Composable () -> Unit) {
    val colorScheme = VirexDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Set status bar and navigation bar colors
            window.statusBarColor = AmoledBlack.toArgb()
            window.navigationBarColor = AmoledBlack.toArgb()

            // Configure system bars appearance
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(colorScheme = colorScheme, typography = VirexTypography, content = content)
}
