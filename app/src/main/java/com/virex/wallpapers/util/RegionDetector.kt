package com.virex.wallpapers.util

import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import com.virex.wallpapers.data.local.PreferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * Region Detector
 *
 * Detects if user is in Russia by checking:
 * 1. VPN Mode setting (if enabled, always return false = global mode)
 * 2. Device Locale
 * 3. SIM country code
 * 4. IP geolocation (lightweight, last resort)
 *
 * When RU is detected and VPN mode is OFF, Firebase/Pexels/Unsplash are disabled and the app uses
 * GitHub CDN only.
 */
@Singleton
class RegionDetector
@Inject
constructor(
        @ApplicationContext private val context: Context,
        private val preferencesDataStore: PreferencesDataStore
) {

    companion object {
        private const val TAG = "RegionDetector"
        private const val RU_COUNTRY_CODE = "ru"
        private const val IP_CHECK_URL = "https://ipapi.co/json/"
        private const val IP_CHECK_TIMEOUT_SECONDS = 5L
    }

    private val _isRussianRegion = MutableStateFlow<Boolean?>(null)
    val isRussianRegion: StateFlow<Boolean?> = _isRussianRegion.asStateFlow()

    /**
     * Quickly detect region using local signals only (no network). Returns true if locale or SIM
     * indicates Russia.
     */
    fun detectLocalRegion(): Boolean {
        val localeRu = isLocaleRussian()
        val simRu = isSimRussian()
        val result = localeRu || simRu
        _isRussianRegion.value = result
        Log.d(TAG, "Local region detection: locale=$localeRu, sim=$simRu → isRU=$result")
        return result
    }

    /** Full region detection including IP check. Should be called on a background thread. */
    suspend fun detectRegion(): Boolean {
        // Fast local checks first
        if (isLocaleRussian() || isSimRussian()) {
            _isRussianRegion.value = true
            Log.d(TAG, "Region detected as RU (local signals)")
            return true
        }

        // Fallback: IP geolocation check
        val ipRussian = checkIpRegion()
        _isRussianRegion.value = ipRussian
        Log.d(TAG, "Region detected via IP: isRU=$ipRussian")
        return ipRussian
    }

    /**
     * Check if blocked services should be disabled.
     *
     * Always returns false - all sources are available via our backend proxy. The backend fetches
     * from external APIs, so no direct connections needed.
     */
    fun shouldDisableBlockedServices(): Boolean {
        // Always return false - app works through backend proxy
        return false
    }

    private fun isLocaleRussian(): Boolean {
        val locale = Locale.getDefault()
        return locale.country.equals(RU_COUNTRY_CODE, ignoreCase = true) ||
                locale.language.equals(RU_COUNTRY_CODE, ignoreCase = true)
    }

    private fun isSimRussian(): Boolean {
        return try {
            val telephonyManager =
                    context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            val simCountry = telephonyManager?.simCountryIso?.lowercase()
            val networkCountry = telephonyManager?.networkCountryIso?.lowercase()
            simCountry == RU_COUNTRY_CODE || networkCountry == RU_COUNTRY_CODE
        } catch (e: Exception) {
            Log.w(TAG, "Failed to check SIM country", e)
            false
        }
    }

    private suspend fun checkIpRegion(): Boolean =
            withContext(Dispatchers.IO) {
                try {
                    val client =
                            OkHttpClient.Builder()
                                    .connectTimeout(IP_CHECK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                                    .readTimeout(IP_CHECK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                                    .build()

                    val request = Request.Builder().url(IP_CHECK_URL).get().build()

                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (body != null) {
                            val json = JSONObject(body)
                            val countryCode = json.optString("country_code", "")
                            return@withContext countryCode.equals(
                                    RU_COUNTRY_CODE,
                                    ignoreCase = true
                            )
                        }
                    }
                    false
                } catch (e: Exception) {
                    Log.w(TAG, "IP region check failed", e)
                    false
                }
            }
}
