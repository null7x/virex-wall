package com.virex.wallpapers.data.local

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/** Supported language options for the app */
enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    SYSTEM("system", "System", "System"),
    ENGLISH("en", "English", "English"),
    RUSSIAN("ru", "Russian", "Русский"),
    TURKISH("tr", "Turkish", "Türkçe"),
    HINDI("hi", "Hindi", "हिन्दी");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return entries.find { it.code == code } ?: SYSTEM
        }
    }
}

/**
 * Manages app localization and language switching
 *
 * Uses Android 13+ per-app language settings when available, falls back to AppCompatDelegate for
 * older versions.
 */
@Singleton
class LocaleHelper
@Inject
constructor(
        @ApplicationContext private val context: Context,
        private val preferencesDataStore: PreferencesDataStore
) {

    /** Get available languages for the app */
    fun getAvailableLanguages(): List<AppLanguage> = AppLanguage.entries.toList()

    /** Get current language setting */
    fun getCurrentLanguage(): AppLanguage {
        return try {
            runBlocking(Dispatchers.IO) {
                val savedCode = preferencesDataStore.appLanguage.first()
                AppLanguage.fromCode(savedCode)
            }
        } catch (e: Exception) {
            AppLanguage.SYSTEM
        }
    }

    /**
     * Set the app language
     * @param language The language to set (SYSTEM for system default)
     */
    suspend fun setLanguage(language: AppLanguage) {
        val code = if (language == AppLanguage.SYSTEM) null else language.code
        preferencesDataStore.setAppLanguage(code)
        applyLanguage(language)
    }

    /** Apply language setting to the app */
    fun applyLanguage(language: AppLanguage) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Use Android 13+ per-app language API
                val localeManager = context.getSystemService(LocaleManager::class.java)
                if (language == AppLanguage.SYSTEM) {
                    localeManager?.applicationLocales = LocaleList.getEmptyLocaleList()
                } else {
                    localeManager?.applicationLocales = LocaleList.forLanguageTags(language.code)
                }
            } else {
                // Fallback for older Android versions
                val localeList =
                        if (language == AppLanguage.SYSTEM) {
                            LocaleListCompat.getEmptyLocaleList()
                        } else {
                            LocaleListCompat.forLanguageTags(language.code)
                        }
                AppCompatDelegate.setApplicationLocales(localeList)
            }
        } catch (e: Exception) {
            // Silently fail - language won't be applied
        }
    }

    /**
     * Initialize language from stored preference Call this in Application.onCreate() or
     * Activity.onCreate()
     */
    fun initializeLanguage() {
        try {
            val language = getCurrentLanguage()
            if (language != AppLanguage.SYSTEM) {
                applyLanguage(language)
            }
        } catch (e: Exception) {
            // Silently fail - will use system default
        }
    }

    /** Get locale for a specific language */
    fun getLocale(language: AppLanguage): Locale {
        return if (language == AppLanguage.SYSTEM) {
            Locale.getDefault()
        } else {
            Locale(language.code)
        }
    }
}
