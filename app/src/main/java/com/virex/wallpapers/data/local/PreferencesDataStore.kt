package com.virex.wallpapers.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.virex.wallpapers.billing.ProStatus
import com.virex.wallpapers.data.model.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by
        preferencesDataStore(name = "virex_preferences")

/**
 * DataStore manager for user preferences
 *
 * Handles persistent storage of user settings and app state.
 */
@Singleton
class PreferencesDataStore @Inject constructor(@ApplicationContext private val context: Context) {

    private object PreferencesKeys {
        val IS_PRO = booleanPreferencesKey("is_pro")
        val AUTO_SET_WALLPAPER = booleanPreferencesKey("auto_set_wallpaper")
        val DOWNLOAD_ONLY_ON_WIFI = booleanPreferencesKey("download_only_on_wifi")
        val SHOW_PREMIUM_BADGE = booleanPreferencesKey("show_premium_badge")
        val CACHE_SIZE = longPreferencesKey("cache_size")
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
        val HAS_SEEN_PRO_PROMO = booleanPreferencesKey("has_seen_pro_promo")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val VPN_MODE_ENABLED = booleanPreferencesKey("vpn_mode_enabled")
    }

    /** Get user preferences as Flow - combines DataStore with ProStatus */
    val userPreferences: Flow<UserPreferences> =
            combine(
                context.dataStore.data,
                ProStatus.isProFlow
            ) { preferences, isProFromBilling ->
                UserPreferences(
                        isPro = isProFromBilling,
                        autoSetWallpaper = preferences[PreferencesKeys.AUTO_SET_WALLPAPER] ?: false,
                        downloadOnlyOnWifi = preferences[PreferencesKeys.DOWNLOAD_ONLY_ON_WIFI]
                                        ?: true,
                        showPremiumBadge = preferences[PreferencesKeys.SHOW_PREMIUM_BADGE] ?: true,
                        cacheSize = preferences[PreferencesKeys.CACHE_SIZE] ?: 0L,
                        lastSyncTime = preferences[PreferencesKeys.LAST_SYNC_TIME] ?: 0L
                )
            }

    /** Check if user has PRO status - uses ProStatus as single source of truth */
    val isPro: Flow<Boolean> = ProStatus.isProFlow

    /** Check if user has seen onboarding */
    val hasSeenOnboarding: Flow<Boolean> =
            context.dataStore.data.map { preferences ->
                preferences[PreferencesKeys.HAS_SEEN_ONBOARDING] ?: false
            }

    /** Set PRO status */
    suspend fun setProStatus(isPro: Boolean) {
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.IS_PRO] = isPro }
    }

    /** Set auto wallpaper setting */
    suspend fun setAutoSetWallpaper(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_SET_WALLPAPER] = enabled
        }
    }

    /** Set download on WiFi only setting */
    suspend fun setDownloadOnlyOnWifi(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DOWNLOAD_ONLY_ON_WIFI] = enabled
        }
    }

    /** Update cache size */
    suspend fun updateCacheSize(size: Long) {
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.CACHE_SIZE] = size }
    }

    /** Update last sync time */
    suspend fun updateLastSyncTime() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SYNC_TIME] = System.currentTimeMillis()
        }
    }

    /** Mark onboarding as seen */
    suspend fun setHasSeenOnboarding(seen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_SEEN_ONBOARDING] = seen
        }
    }

    /** Mark PRO promo as seen */
    suspend fun setHasSeenProPromo(seen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_SEEN_PRO_PROMO] = seen
        }
    }

    /** Get app language preference (null = system default) */
    val appLanguage: Flow<String?> =
            context.dataStore.data.map { preferences ->
                preferences[PreferencesKeys.APP_LANGUAGE]
            }

    /** Set app language preference (null = system default) */
    suspend fun setAppLanguage(languageCode: String?) {
        context.dataStore.edit { preferences ->
            if (languageCode == null) {
                preferences.remove(PreferencesKeys.APP_LANGUAGE)
            } else {
                preferences[PreferencesKeys.APP_LANGUAGE] = languageCode
            }
        }
    }

    /** VPN mode enabled - bypasses Russia detection and loads ALL sources */
    val vpnModeEnabled: Flow<Boolean> =
            context.dataStore.data.map { preferences ->
                preferences[PreferencesKeys.VPN_MODE_ENABLED] ?: false
            }

    /** Set VPN mode */
    suspend fun setVpnModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VPN_MODE_ENABLED] = enabled
        }
    }

    /** Get VPN mode synchronously (for RegionDetector) */
    fun getVpnModeEnabledSync(): Boolean {
        return runBlocking {
            context.dataStore.data.map { it[PreferencesKeys.VPN_MODE_ENABLED] ?: false }.first()
        }
    }

    /** Clear all preferences */
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
