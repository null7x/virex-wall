package com.virex.wallpapers.ui.premium

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * VIREX Premium Design System
 * 
 * Futuristic • Minimal • AMOLED • Glassmorphism • Neon
 */
object PremiumTheme {
    
    // ═══════════════════════════════════════════════════════════════
    // COLORS - Pure AMOLED
    // ═══════════════════════════════════════════════════════════════
    
    val Background = Color(0xFF000000)
    val Surface = Color(0xFF0A0A0A)
    val SurfaceElevated = Color(0xFF121212)
    val SurfaceCard = Color(0xFF151515)
    
    // Glass effects
    val GlassPrimary = Color(0x1AFFFFFF)
    val GlassSecondary = Color(0x0DFFFFFF)
    val GlassBorder = Color(0x33FFFFFF)
    val GlassHighlight = Color(0x4DFFFFFF)
    
    // Neon Accents
    val NeonBlue = Color(0xFF00D4FF)
    val NeonPurple = Color(0xFFBB86FC)
    val NeonPink = Color(0xFFFF6B9D)
    val NeonCyan = Color(0xFF00FFFF)
    val NeonViolet = Color(0xFF8B5CF6)
    
    // Gradients
    val GradientStart = Color(0xFF00D4FF)  // Electric Blue
    val GradientMid = Color(0xFF8B5CF6)     // Violet
    val GradientEnd = Color(0xFFBB86FC)     // Neon Purple
    
    // AI Gradient
    val AIGradientStart = Color(0xFFFF6B9D)
    val AIGradientMid = Color(0xFFBB86FC)
    val AIGradientEnd = Color(0xFF00D4FF)
    
    // Pro/Premium
    val ProGold = Color(0xFFFFD700)
    val ProGradientStart = Color(0xFFFFD700)
    val ProGradientEnd = Color(0xFFFFA500)
    
    // Text
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xB3FFFFFF)
    val TextTertiary = Color(0x66FFFFFF)
    val TextDisabled = Color(0x33FFFFFF)
    
    // Status
    val Success = Color(0xFF00E676)
    val Error = Color(0xFFFF5252)
    val Warning = Color(0xFFFFD740)
    
    // ═══════════════════════════════════════════════════════════════
    // DIMENSIONS
    // ═══════════════════════════════════════════════════════════════
    
    val CornerRadiusSmall = 12.dp
    val CornerRadiusMedium = 20.dp
    val CornerRadiusLarge = 28.dp
    val CornerRadiusXLarge = 36.dp
    
    val CardElevation = 8.dp
    val GlassBlurRadius = 20.dp
    
    val SpacingXS = 4.dp
    val SpacingS = 8.dp
    val SpacingM = 16.dp
    val SpacingL = 24.dp
    val SpacingXL = 32.dp
    val SpacingXXL = 48.dp
    
    // ═══════════════════════════════════════════════════════════════
    // ANIMATION DURATIONS
    // ═══════════════════════════════════════════════════════════════
    
    const val AnimFast = 150
    const val AnimMedium = 300
    const val AnimSlow = 500
    const val AnimVerySlow = 800
    
    // ═══════════════════════════════════════════════════════════════
    // CARD SIZES
    // ═══════════════════════════════════════════════════════════════
    
    val CardHeightSmall = 180.dp
    val CardHeightMedium = 240.dp
    val CardHeightLarge = 320.dp
    val CardHeightHero = 400.dp
    
    val CategoryCardHeight = 100.dp
    val FeaturedCardHeight = 280.dp
}
