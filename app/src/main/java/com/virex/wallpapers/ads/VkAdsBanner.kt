package com.virex.wallpapers.ads

import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.my.target.ads.MyTargetView
import com.my.target.common.models.IAdLoadingError
import com.virex.wallpapers.billing.ProStatus
import com.virex.wallpapers.ui.theme.AmoledBlack

private const val TAG = "VK_ADS_BANNER"

/**
 * VK Ads Banner Composable
 *
 * Production-ready banner that:
 * - Uses correct SLOT_ID (1974334)
 * - Handles lifecycle properly
 * - Logs all events via Log.e
 * - Respects PRO status
 */
@Composable
fun VkAdsBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Don't show for PRO users
    if (ProStatus.isPro(context)) {
        Log.e(TAG, "👑 PRO user - banner hidden")
        return
    }

    var adView by remember { mutableStateOf<MyTargetView?>(null) }
    var isAdLoaded by remember { mutableStateOf(false) }

    // Lifecycle observer
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_DESTROY -> {
                    Log.e(TAG, "🗑️ ON_DESTROY: Destroying banner")
                    adView?.destroy()
                    adView = null
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            adView?.destroy()
            adView = null
        }
    }

    // Preload ad in an invisible container, show only when loaded
    if (!isAdLoaded) {
        // Invisible loader — 0dp height, loads the ad in background
        Box(modifier = Modifier.fillMaxWidth().height(0.dp)) {
            AndroidView(
                    factory = { ctx ->
                        Log.e(TAG, "🏗️ Creating banner loader (invisible)")
                        FrameLayout(ctx).apply {
                            layoutParams =
                                    ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            1 // minimal 1px to allow ad SDK to initialize
                                    )
                            val newAdView =
                                    MyTargetView(ctx).apply {
                                        setSlotId(VkAdsManager.BANNER_SLOT_ID)
                                    }
                            newAdView.setListener(
                                    object : MyTargetView.MyTargetViewListener {
                                        override fun onLoad(view: MyTargetView) {
                                            Log.e(TAG, "✅ Banner loaded successfully!")
                                            isAdLoaded = true
                                        }
                                        override fun onNoAd(
                                                error: IAdLoadingError,
                                                view: MyTargetView
                                        ) {
                                            Log.e(
                                                    TAG,
                                                    "❌ No fill: ${error.message} (code: ${error.code})"
                                            )
                                            isAdLoaded = false
                                        }
                                        override fun onClick(view: MyTargetView) {
                                            Log.e(TAG, "👆 Banner clicked")
                                        }
                                        override fun onShow(view: MyTargetView) {
                                            Log.e(TAG, "👁️ Banner shown - IMPRESSION COUNTED")
                                        }
                                    }
                            )
                            addView(newAdView)
                            adView = newAdView
                            post { newAdView.load() }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
            )
        }
    } else {
        // Banner container — visible only when ad is loaded
        Box(modifier = modifier.fillMaxWidth().height(50.dp).background(AmoledBlack)) {
            AndroidView(
                    factory = { ctx ->
                        Log.e(TAG, "🏗️ Creating visible banner container")
                        val density = ctx.resources.displayMetrics.density
                        val heightPx = (50 * density).toInt()
                        FrameLayout(ctx).apply {
                            layoutParams =
                                    ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            heightPx
                                    )
                            setBackgroundColor(android.graphics.Color.BLACK)
                            adView?.let { view ->
                                (view.parent as? ViewGroup)?.removeView(view)
                                val adParams =
                                        FrameLayout.LayoutParams(
                                                FrameLayout.LayoutParams.MATCH_PARENT,
                                                heightPx
                                        )
                                addView(view, adParams)
                            }
                        }
                    },
                    update = { container ->
                        Log.e(TAG, "📐 Container update: ${container.width}x${container.height}")
                    },
                    modifier = Modifier.fillMaxSize()
            )
        }
    }
}
