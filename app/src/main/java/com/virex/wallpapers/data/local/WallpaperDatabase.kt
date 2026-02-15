package com.virex.wallpapers.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.virex.wallpapers.data.model.CachedWallpaper
import com.virex.wallpapers.data.model.CategoryPreference
import com.virex.wallpapers.data.model.CdnWallpaper
import com.virex.wallpapers.data.model.DownloadHistory
import com.virex.wallpapers.data.model.FavoriteWallpaper
import com.virex.wallpapers.data.model.InteractionType
import com.virex.wallpapers.data.model.SyncCategory
import com.virex.wallpapers.data.model.SyncStatus
import com.virex.wallpapers.data.model.SyncedWallpaper
import com.virex.wallpapers.data.model.UserInteraction
import com.virex.wallpapers.data.model.Wallpaper
import com.virex.wallpapers.data.model.WallpaperSource
import com.virex.wallpapers.data.model.WeeklyStats

/**
 * Room Database for VIREX Wallpapers
 *
 * Stores:
 * - Cached wallpapers for offline access
 * - User favorites
 * - Download history
 * - Synced wallpapers from external APIs
 * - Sync status
 * - User interactions for recommendations
 * - Category preferences
 * - Weekly stats for "Popular this week"
 */
@Database(
        entities =
                [
                        Wallpaper::class,
                        FavoriteWallpaper::class,
                        CachedWallpaper::class,
                        DownloadHistory::class,
                        SyncedWallpaper::class,
                        SyncStatus::class,
                        UserInteraction::class,
                        CategoryPreference::class,
                        WeeklyStats::class,
                        CdnWallpaper::class],
        version = 5,
        exportSchema = true
)
@TypeConverters(Converters::class)
abstract class WallpaperDatabase : RoomDatabase() {

    abstract fun wallpaperDao(): WallpaperDao

    abstract fun syncedWallpaperDao(): SyncedWallpaperDao

    abstract fun recommendationDao(): RecommendationDao

    abstract fun cdnWallpaperDao(): CdnWallpaperDao

    companion object {
        const val DATABASE_NAME = "virex_wallpapers_db"
    }
}

/** Type converters for Room */
class Converters {

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }

    // SyncCategory converters
    @TypeConverter
    fun fromSyncCategory(category: SyncCategory): String {
        return category.name
    }

    @TypeConverter
    fun toSyncCategory(value: String): SyncCategory {
        return SyncCategory.valueOf(value)
    }

    // WallpaperSource converters
    @TypeConverter
    fun fromWallpaperSource(source: WallpaperSource): String {
        return source.name
    }

    @TypeConverter
    fun toWallpaperSource(value: String): WallpaperSource {
        return WallpaperSource.valueOf(value)
    }

    // InteractionType converters
    @TypeConverter
    fun fromInteractionType(type: InteractionType): String {
        return type.name
    }

    @TypeConverter
    fun toInteractionType(value: String): InteractionType {
        return InteractionType.valueOf(value)
    }
}
