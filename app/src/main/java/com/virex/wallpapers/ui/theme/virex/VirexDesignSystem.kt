package com.virex.wallpapers.ui.theme.virex

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ═══════════════════════════════════════════════════════════════════════════════
// VIREX DESIGN SYSTEM — UNIFIED COLORS
// ═══════════════════════════════════════════════════════════════════════════════

@Immutable
object VirexColors {
    // Background
    val Background = Color(0xFF000000)          // Pure AMOLED black
    val Surface = Color(0xFF0A0A0A)             // Slightly elevated surface
    val SurfaceElevated = Color(0xFF121212)     // Cards, dialogs
    val SurfaceGlass = Color(0xFF1A1A2E)        // Glass morphism
    
    // Glass effects
    val GlassPrimary = Color(0xFF0D0D1A).copy(alpha = 0.85f)
    val GlassSecondary = Color(0xFF1A1A2E).copy(alpha = 0.75f)
    val GlassBorder = Color.White.copy(alpha = 0.08f)
    
    // Primary palette (Neon)
    val Primary = Color(0xFF00D4FF)             // Neon Blue
    val PrimaryVariant = Color(0xFFBB86FC)      // Neon Purple
    val Accent = Color(0xFFFF6EC7)              // Neon Pink
    
    // Status colors
    val Success = Color(0xFF00E676)
    val Warning = Color(0xFFFFAB00)
    val Error = Color(0xFFFF5252)
    
    // PRO
    val ProGold = Color(0xFFFFD700)
    val ProGradientStart = Color(0xFFFFD700)
    val ProGradientEnd = Color(0xFFFFA000)
    
    // Text
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFB3B3B3)
    val TextTertiary = Color(0xFF666666)
    val TextDisabled = Color(0xFF404040)
    
    // Gradients
    val PrimaryGradient = Brush.linearGradient(
        colors = listOf(Primary, PrimaryVariant, Accent)
    )
    
    val ProGradient = Brush.linearGradient(
        colors = listOf(ProGradientStart, ProGradientEnd)
    )
    
    val GlassGradient = Brush.verticalGradient(
        colors = listOf(GlassPrimary, GlassSecondary)
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// VIREX DESIGN SYSTEM — UNIFIED TYPOGRAPHY
// ═══════════════════════════════════════════════════════════════════════════════

@Immutable
object VirexTypography {
    // Display - App name, hero text
    val LargeTitle = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 2.sp,
        color = VirexColors.TextPrimary
    )
    
    // Screen titles - TopAppBar
    val ScreenTitle = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        color = VirexColors.TextPrimary
    )
    
    // Section headers
    val SectionTitle = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = VirexColors.TextPrimary
    )
    
    // Card titles
    val CardTitle = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = VirexColors.TextPrimary
    )
    
    // Body text
    val Body = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        color = VirexColors.TextSecondary
    )
    
    // Small body
    val BodySmall = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        color = VirexColors.TextSecondary
    )
    
    // Captions, labels
    val Caption = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = VirexColors.TextTertiary
    )
    
    // Badges, chips
    val Label = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        color = VirexColors.TextPrimary
    )
    
    // Button text
    val Button = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = VirexColors.TextPrimary
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// VIREX DESIGN SYSTEM — UNIFIED SPACING
// ═══════════════════════════════════════════════════════════════════════════════

@Immutable
object VirexSpacing {
    val xxxs: Dp = 2.dp
    val xxs: Dp = 4.dp
    val xs: Dp = 8.dp
    val sm: Dp = 12.dp
    val md: Dp = 16.dp
    val lg: Dp = 20.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
    val xxxl: Dp = 48.dp
    
    // Screen paddings
    val screenHorizontal: Dp = 16.dp
    val screenTop: Dp = 16.dp
    val screenBottom: Dp = 100.dp  // Space for bottom bar + FAB
    
    // Card internal padding
    val cardPadding: Dp = 16.dp
    
    // Grid spacing
    val gridSpacing: Dp = 12.dp
}

// ═══════════════════════════════════════════════════════════════════════════════
// VIREX DESIGN SYSTEM — UNIFIED SHAPES
// ═══════════════════════════════════════════════════════════════════════════════

@Immutable
object VirexShapes {
    val radiusXs: Dp = 8.dp
    val radiusSm: Dp = 12.dp
    val radiusMd: Dp = 16.dp
    val radiusLg: Dp = 20.dp
    val radiusXl: Dp = 24.dp
    val radiusFull: Dp = 100.dp
    
    // Standard component radii
    val card: Dp = 24.dp
    val button: Dp = 24.dp
    val chip: Dp = 100.dp
    val bottomBar: Dp = 28.dp
    val fab: Dp = 28.dp
    val dialog: Dp = 28.dp
    val searchBar: Dp = 20.dp
}

// ═══════════════════════════════════════════════════════════════════════════════
// VIREX DESIGN SYSTEM — UNIFIED DIMENSIONS
// ═══════════════════════════════════════════════════════════════════════════════

@Immutable
object VirexDimens {
    // Icons
    val iconXs: Dp = 16.dp
    val iconSm: Dp = 20.dp
    val iconMd: Dp = 24.dp
    val iconLg: Dp = 28.dp
    val iconXl: Dp = 32.dp
    
    // Top bar
    val topBarHeight: Dp = 64.dp
    val topBarIconSize: Dp = 24.dp
    
    // Bottom bar
    val bottomBarHeight: Dp = 72.dp
    val bottomBarIconSize: Dp = 26.dp
    
    // FAB
    val fabSize: Dp = 72.dp
    val fabIconSize: Dp = 28.dp
    val fabOffset: Dp = 36.dp
    
    // Cards
    val wallpaperCardHeight: Dp = 280.dp
    val categoryCardHeight: Dp = 140.dp
    val featuredCardHeight: Dp = 200.dp
    
    // Buttons
    val buttonHeight: Dp = 52.dp
    val chipHeight: Dp = 36.dp
    
    // Avatar / Logo
    val logoSize: Dp = 36.dp
}

// ═══════════════════════════════════════════════════════════════════════════════
// VIREX DESIGN SYSTEM — ANIMATIONS
// ═══════════════════════════════════════════════════════════════════════════════

@Immutable
object VirexAnimations {
    const val durationFast = 150
    const val durationMedium = 300
    const val durationSlow = 500
    
    const val scalePressed = 0.96f
    const val scaleNormal = 1f
}

// ═══════════════════════════════════════════════════════════════════════════════
// COMPOSITION LOCAL — ACCESS FROM ANYWHERE
// ═══════════════════════════════════════════════════════════════════════════════

val LocalVirexColors = staticCompositionLocalOf { VirexColors }
val LocalVirexTypography = staticCompositionLocalOf { VirexTypography }
val LocalVirexSpacing = staticCompositionLocalOf { VirexSpacing }
val LocalVirexShapes = staticCompositionLocalOf { VirexShapes }
val LocalVirexDimens = staticCompositionLocalOf { VirexDimens }

// ═══════════════════════════════════════════════════════════════════════════════
// VIREX THEME PROVIDER
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun VirexTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalVirexColors provides VirexColors,
        LocalVirexTypography provides VirexTypography,
        LocalVirexSpacing provides VirexSpacing,
        LocalVirexShapes provides VirexShapes,
        LocalVirexDimens provides VirexDimens,
        content = content
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// ACCESSOR OBJECT
// ═══════════════════════════════════════════════════════════════════════════════

object Virex {
    val colors: VirexColors
        @Composable
        get() = LocalVirexColors.current
    
    val typography: VirexTypography
        @Composable
        get() = LocalVirexTypography.current
    
    val spacing: VirexSpacing
        @Composable
        get() = LocalVirexSpacing.current
    
    val shapes: VirexShapes
        @Composable
        get() = LocalVirexShapes.current
    
    val dimens: VirexDimens
        @Composable
        get() = LocalVirexDimens.current
}
