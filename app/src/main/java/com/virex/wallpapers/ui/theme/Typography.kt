package com.virex.wallpapers.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Using system fonts for better performance and smaller APK size
// You can add custom fonts by creating font files in res/font/

val VirexFontFamily = FontFamily.Default

val VirexTypography =
        Typography(
                // Display styles - for hero text and large titles
                displayLarge =
                        TextStyle(
                                fontFamily = VirexFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 57.sp,
                                lineHeight = 64.sp,
                                letterSpacing = (-0.25).sp,
                                color = TextPrimary
                        ),
                displayMedium =
                        TextStyle(
                                fontFamily = VirexFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 45.sp,
                                lineHeight = 52.sp,
                                letterSpacing = 0.sp,
                                color = TextPrimary
                        ),
                displaySmall =
                        TextStyle(
                                fontFamily = VirexFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp,
                                lineHeight = 44.sp,
                                letterSpacing = 0.sp,
                                color = TextPrimary
                        ),

                // Headline styles - for section headers
                headlineLarge =
                        TextStyle(
                                fontFamily = VirexFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 32.sp,
                                lineHeight = 40.sp,
                                letterSpacing = 0.sp,
                                color = TextPrimary
                        ),
                headlineMedium =
                        TextStyle(
                                fontFamily = VirexFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 28.sp,
                                lineHeight = 36.sp,
                                letterSpacing = 0.sp,
                                color = TextPrimary
                        ),
                headlineSmall =
                        TextStyle(
                                fontFamily = VirexFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 24.sp,
                                lineHeight = 32.sp,
                                letterSpacing = 0.sp,
                                color = TextPrimary
                        ),

                // Title styles - for cards and list items
                titleLarge =
                        TextStyle(
                                fontFamily = VirexFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 22.sp,
                                lineHeight = 28.sp,
                                letterSpacing = 0.sp,
                                color = TextPrimary
                        ),
                titleMedium =
                        TextStyle(
                                fontFamily = VirexFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                letterSpacing = 0.15.sp,
                                color = TextPrimary
                        ),
                titleSmall =
                        TextStyle(
                                fontFamily = VirexFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                letterSpacing = 0.1.sp,
                                color = TextPrimary
                        ),

                // Body styles - for content text
                bodyLarge =
                        TextStyle(
                                fontFamily = VirexFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                letterSpacing = 0.5.sp,
                                color = TextSecondary
                        ),
                bodyMedium =
                        TextStyle(
                                fontFamily = VirexFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                letterSpacing = 0.25.sp,
                                color = TextSecondary
                        ),
                bodySmall =
                        TextStyle(
                                fontFamily = VirexFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                letterSpacing = 0.4.sp,
                                color = TextSecondary
                        ),

                // Label styles - for buttons and chips
                labelLarge =
                        TextStyle(
                                fontFamily = VirexFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                letterSpacing = 0.1.sp,
                                color = TextPrimary
                        ),
                labelMedium =
                        TextStyle(
                                fontFamily = VirexFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                letterSpacing = 0.5.sp,
                                color = TextPrimary
                        ),
                labelSmall =
                        TextStyle(
                                fontFamily = VirexFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                letterSpacing = 0.5.sp,
                                color = TextSecondary
                        )
        )
