package com.virex.wallpapers.ui.screens.generator

import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virex.wallpapers.data.local.PreferencesDataStore
import com.virex.wallpapers.data.model.WallpaperTarget
import com.virex.wallpapers.generator.AccentColors
import com.virex.wallpapers.generator.ProceduralGenerator
import com.virex.wallpapers.generator.WallpaperStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Generator UI State */
data class GeneratorUiState(
        val selectedStyle: WallpaperStyle = WallpaperStyle.default,
        val accentColor: Color = AccentColors.presets.first(),
        val intensity: Float = 0.5f,
        val seed: Long = System.currentTimeMillis(),
        val generatedBitmap: Bitmap? = null,
        val isGenerating: Boolean = false,
        val isSaving: Boolean = false,
        val isSettingWallpaper: Boolean = false
)

/** ViewModel for AI Generator Screen */
@HiltViewModel
class GeneratorViewModel
@Inject
constructor(
        @ApplicationContext private val context: Context,
        private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(GeneratorUiState())
    val uiState: StateFlow<GeneratorUiState> = _uiState.asStateFlow()

    private val _message = MutableSharedFlow<String>()
    val message: SharedFlow<String> = _message.asSharedFlow()

    /** PRO status from preferences */
    val isPro: StateFlow<Boolean> =
            preferencesDataStore
                    .userPreferences
                    .map { it.isPro }
                    .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /** Update selected style */
    fun selectStyle(style: WallpaperStyle) {
        _uiState.value =
                _uiState.value.copy(selectedStyle = style, accentColor = style.defaultAccentColor)
    }

    /** Update accent color */
    fun selectAccentColor(color: Color) {
        _uiState.value = _uiState.value.copy(accentColor = color)
    }

    /** Update intensity */
    fun setIntensity(intensity: Float) {
        _uiState.value = _uiState.value.copy(intensity = intensity.coerceIn(0f, 1f))
    }

    /** Generate new random seed */
    fun randomizeSeed() {
        _uiState.value = _uiState.value.copy(seed = System.currentTimeMillis())
    }

    /** Generate wallpaper with current settings */
    fun generate() {
        if (_uiState.value.isGenerating) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGenerating = true)

            try {
                // Get screen dimensions for optimal size
                val displayMetrics = context.resources.displayMetrics
                val width = displayMetrics.widthPixels
                val height = displayMetrics.heightPixels

                val bitmap =
                        ProceduralGenerator.generate(
                                style = _uiState.value.selectedStyle,
                                accentColor = _uiState.value.accentColor,
                                intensity = _uiState.value.intensity,
                                seed = _uiState.value.seed,
                                width = width,
                                height = height
                        )

                _uiState.value = _uiState.value.copy(generatedBitmap = bitmap, isGenerating = false)

                _message.emit("Wallpaper generated!")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isGenerating = false)
                _message.emit("Generation failed: ${e.message}")
            }
        }
    }

    /** Save generated wallpaper to gallery */
    fun saveToGallery() {
        val bitmap = _uiState.value.generatedBitmap ?: return
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            try {
                val filename =
                        "VIREX_${_uiState.value.selectedStyle.name}_${System.currentTimeMillis()}.png"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10+ - use MediaStore
                    val contentValues =
                            ContentValues().apply {
                                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                                put(
                                        MediaStore.Images.Media.RELATIVE_PATH,
                                        Environment.DIRECTORY_PICTURES + "/VIREX"
                                )
                                put(MediaStore.Images.Media.IS_PENDING, 1)
                            }

                    val uri =
                            context.contentResolver.insert(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    contentValues
                            )

                    uri?.let { imageUri ->
                        context.contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        }

                        contentValues.clear()
                        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                        context.contentResolver.update(imageUri, contentValues, null, null)

                        _message.emit("Saved to Pictures/VIREX")
                    }
                            ?: throw IOException("Failed to create file")
                } else {
                    // Legacy storage
                    @Suppress("DEPRECATION")
                    val picturesDir =
                            Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_PICTURES
                            )
                    val virexDir = File(picturesDir, "VIREX")
                    if (!virexDir.exists()) virexDir.mkdirs()

                    val file = File(virexDir, filename)
                    FileOutputStream(file).use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }

                    // Scan the file so it appears in gallery
                    @Suppress("DEPRECATION")
                    android.media.MediaScannerConnection.scanFile(
                            context,
                            arrayOf(file.absolutePath),
                            arrayOf("image/png"),
                            null
                    )

                    _message.emit("Saved to Pictures/VIREX")
                }
            } catch (e: Exception) {
                _message.emit("Save failed: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }

    /** Set generated wallpaper as device wallpaper */
    fun setAsWallpaper(target: WallpaperTarget) {
        val bitmap = _uiState.value.generatedBitmap ?: return
        if (_uiState.value.isSettingWallpaper) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSettingWallpaper = true)

            try {
                val wallpaperManager = WallpaperManager.getInstance(context)

                val which =
                        when (target) {
                            WallpaperTarget.HOME_SCREEN -> WallpaperManager.FLAG_SYSTEM
                            WallpaperTarget.LOCK_SCREEN -> WallpaperManager.FLAG_LOCK
                            WallpaperTarget.BOTH ->
                                    WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                        }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    wallpaperManager.setBitmap(bitmap, null, true, which)
                } else {
                    wallpaperManager.setBitmap(bitmap)
                }

                val targetName =
                        when (target) {
                            WallpaperTarget.HOME_SCREEN -> "home screen"
                            WallpaperTarget.LOCK_SCREEN -> "lock screen"
                            WallpaperTarget.BOTH -> "home and lock screen"
                        }
                _message.emit("Wallpaper set as $targetName!")
            } catch (e: Exception) {
                _message.emit("Failed to set wallpaper: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isSettingWallpaper = false)
            }
        }
    }

    /** Clear generated bitmap to free memory */
    fun clearBitmap() {
        _uiState.value.generatedBitmap?.recycle()
        _uiState.value = _uiState.value.copy(generatedBitmap = null)
    }

    override fun onCleared() {
        super.onCleared()
        _uiState.value.generatedBitmap?.recycle()
    }
}
