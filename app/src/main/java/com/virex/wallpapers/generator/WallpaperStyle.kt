package com.virex.wallpapers.generator

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.virex.wallpapers.R

/**
 * Available wallpaper generation styles.
 *
 * Each style uses different procedural algorithms to create unique patterns.
 */
enum class WallpaperStyle(
        val displayName: String,
        @StringRes val descriptionRes: Int,
        val defaultAccentColor: Color
) {
    GEOMETRIC_LINES(
            displayName = "Geometric Lines",
            descriptionRes = R.string.style_geometric_lines_desc,
            defaultAccentColor = Color(0xFF00D4FF) // Neon Blue
    ),
    NEON_GLOW(
            displayName = "Neon Glow",
            descriptionRes = R.string.style_neon_glow_desc,
            defaultAccentColor = Color(0xFF00FF88) // Neon Green
    ),
    PARTICLE_FIELD(
            displayName = "Particle Field",
            descriptionRes = R.string.style_particle_field_desc,
            defaultAccentColor = Color(0xFFFF00FF) // Magenta
    ),
    ABSTRACT_WAVES(
            displayName = "Abstract Waves",
            descriptionRes = R.string.style_abstract_waves_desc,
            defaultAccentColor = Color(0xFF8B5CF6) // Purple
    ),
    DARK_GRADIENT(
            displayName = "Dark Gradient",
            descriptionRes = R.string.style_dark_gradient_desc,
            defaultAccentColor = Color(0xFF00D4FF) // Neon Blue
    ),
    CONSTELLATION(
            displayName = "Constellation",
            descriptionRes = R.string.style_constellation_desc,
            defaultAccentColor = Color(0xFFFFFFFF) // White
    ),
    MINIMAL_SHAPES(
            displayName = "Minimal Shapes",
            descriptionRes = R.string.style_minimal_shapes_desc,
            defaultAccentColor = Color(0xFFFF6B6B) // Coral
    ),
    CIRCUIT_BOARD(
            displayName = "Circuit Board",
            descriptionRes = R.string.style_circuit_board_desc,
            defaultAccentColor = Color(0xFF00FF00) // Green
    ),
    AURORA(
            displayName = "Aurora",
            descriptionRes = R.string.style_aurora_desc,
            defaultAccentColor = Color(0xFF00D4FF) // Cyan
    ),
    FRACTAL_NOISE(
            displayName = "Fractal Noise",
            descriptionRes = R.string.style_fractal_noise_desc,
            defaultAccentColor = Color(0xFFFF8800) // Orange
    );

    companion object {
        val default = GEOMETRIC_LINES
    }
}

/** Preset accent colors for the generator. */
object AccentColors {
    val presets =
            listOf(
                    Color(0xFF00D4FF), // Neon Blue
                    Color(0xFF00FF88), // Neon Green
                    Color(0xFFFF00FF), // Magenta
                    Color(0xFF8B5CF6), // Purple
                    Color(0xFFFF6B6B), // Coral
                    Color(0xFFFFD700), // Gold
                    Color(0xFF00FFFF), // Cyan
                    Color(0xFFFF8800), // Orange
                    Color(0xFFFF1493), // Deep Pink
                    Color(0xFF7FFF00), // Chartreuse
                    Color(0xFFFFFFFF), // White
                    Color(0xFF808080) // Gray
            )
}
