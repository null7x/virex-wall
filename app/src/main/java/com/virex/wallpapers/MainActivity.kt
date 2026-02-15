package com.virex.wallpapers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.virex.wallpapers.ui.navigation.VirexNavHost
import com.virex.wallpapers.ui.theme.AmoledBlack
import com.virex.wallpapers.ui.theme.VirexTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity - Entry point for VIREX Wallpapers
 *
 * Features:
 * - Edge-to-edge display for immersive wallpaper viewing
 * - Splash screen with app icon
 * - Pure AMOLED black theme
 * - Per-app language support via AppCompatDelegate
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        setContent {
            VirexTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = AmoledBlack) { VirexNavHost() }
            }
        }
    }
}
