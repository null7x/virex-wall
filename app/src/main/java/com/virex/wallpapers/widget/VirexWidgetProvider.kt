package com.virex.wallpapers.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.virex.wallpapers.R

/**
 * VIREX Wallpaper Widget Provider
 * 
 * Provides home screen widgets for quick wallpaper actions:
 * - Random AMOLED Wallpaper button
 * - Favorite Wallpaper button
 * 
 * Supports 1x1 and 2x1 widget sizes.
 */
class VirexWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_RANDOM_WALLPAPER = "com.virex.wallpapers.widget.ACTION_RANDOM_WALLPAPER"
        const val ACTION_FAVORITE_WALLPAPER = "com.virex.wallpapers.widget.ACTION_FAVORITE_WALLPAPER"
        const val ACTION_OPEN_APP = "com.virex.wallpapers.widget.ACTION_OPEN_APP"
        
        /**
         * Update all widgets
         */
        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, VirexWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val widgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = widgetManager.getAppWidgetIds(
                ComponentName(context, VirexWidgetProvider::class.java)
            )
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
            context.sendBroadcast(intent)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        when (intent.action) {
            ACTION_RANDOM_WALLPAPER -> {
                // Start service to set random wallpaper
                val serviceIntent = Intent(context, WidgetWallpaperService::class.java).apply {
                    action = WidgetWallpaperService.ACTION_SET_RANDOM
                }
                context.startService(serviceIntent)
            }
            ACTION_FAVORITE_WALLPAPER -> {
                // Start service to set favorite wallpaper
                val serviceIntent = Intent(context, WidgetWallpaperService::class.java).apply {
                    action = WidgetWallpaperService.ACTION_SET_FAVORITE
                }
                context.startService(serviceIntent)
            }
            ACTION_OPEN_APP -> {
                // Open main app
                val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                launchIntent?.let {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    context.startActivity(it)
                }
            }
        }
    }

    override fun onEnabled(context: Context) {
        // Called when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Called when the last widget is disabled
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // Get widget size info
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        
        // Choose layout based on widget size
        val layoutId = if (minWidth >= 110) {
            R.layout.widget_virex_2x1
        } else {
            R.layout.widget_virex_1x1
        }
        
        val views = RemoteViews(context.packageName, layoutId)
        
        // Set up click listeners
        setupClickListeners(context, views, layoutId)
        
        // Update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
    
    private fun setupClickListeners(context: Context, views: RemoteViews, layoutId: Int) {
        // Random wallpaper button
        val randomIntent = Intent(context, VirexWidgetProvider::class.java).apply {
            action = ACTION_RANDOM_WALLPAPER
        }
        val randomPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            randomIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_random_wallpaper, randomPendingIntent)
        
        // For 2x1 layout, also set up favorite button
        if (layoutId == R.layout.widget_virex_2x1) {
            val favoriteIntent = Intent(context, VirexWidgetProvider::class.java).apply {
                action = ACTION_FAVORITE_WALLPAPER
            }
            val favoritePendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                favoriteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_favorite_wallpaper, favoritePendingIntent)
        }
        
        // Logo click opens app
        val openAppIntent = Intent(context, VirexWidgetProvider::class.java).apply {
            action = ACTION_OPEN_APP
        }
        val openAppPendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_logo, openAppPendingIntent)
    }
}
