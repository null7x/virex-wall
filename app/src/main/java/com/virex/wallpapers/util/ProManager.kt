package com.virex.wallpapers.util

import android.content.Context
import android.util.Log
import com.virex.wallpapers.billing.ProStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.StateFlow

/**
 * PRO Manager
 *
 * Exposes PRO user state as a reactive StateFlow. Delegates to ProStatus for persistence.
 *
 * Ready for RuStore Billing integration. Currently uses local state via SharedPreferences.
 */
@Singleton
class ProManager @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private const val TAG = "ProManager"
    }

    /** Observable PRO status. UI observes this to show/hide PRO features. */
    val isProUser: StateFlow<Boolean> = ProStatus.isProFlow

    /** Check if user is PRO (sync call) */
    fun isPro(): Boolean = ProStatus.isPro(context)

    /** Set PRO status (for billing callback) */
    fun setPro(isPro: Boolean, orderId: String? = null) {
        ProStatus.setPro(context, isPro, orderId)
        Log.d(TAG, "PRO status set to $isPro")
    }

    /**
     * Should the full-res image be loaded? Non-PRO users only get thumbnails for PRO wallpapers.
     */
    fun canAccessFullImage(isProWallpaper: Boolean): Boolean {
        return !isProWallpaper || isPro()
    }

    /**
     * Get the appropriate image URL based on PRO status. Returns thumbnail for locked PRO
     * wallpapers.
     */
    fun getAccessibleImageUrl(
            thumbnailUrl: String,
            fullImageUrl: String,
            isProWallpaper: Boolean
    ): String {
        return if (canAccessFullImage(isProWallpaper)) {
            fullImageUrl
        } else {
            thumbnailUrl
        }
    }
}
