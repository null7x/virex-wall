package com.virex.wallpapers.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Favorite wallpaper entity for Room database */
@Entity(tableName = "favorites")
data class FavoriteWallpaper(
        @PrimaryKey val wallpaperId: String,
        val addedAt: Long = System.currentTimeMillis()
)

/** Cached wallpaper entity for offline access (PRO feature) */
@Entity(tableName = "cached_wallpapers")
data class CachedWallpaper(
        @PrimaryKey val wallpaperId: String,
        val localPath: String,
        val cachedAt: Long = System.currentTimeMillis(),
        val fileSize: Long = 0L
)

/** Download history entity */
@Entity(tableName = "download_history")
data class DownloadHistory(
        @PrimaryKey(autoGenerate = true) val id: Long = 0,
        val wallpaperId: String,
        val downloadedAt: Long = System.currentTimeMillis()
)
