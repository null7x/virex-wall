package com.virex.wallpapers.di

import android.content.Context
import androidx.room.Room
import com.virex.wallpapers.data.local.CdnWallpaperDao
import com.virex.wallpapers.data.local.PreferencesDataStore
import com.virex.wallpapers.data.local.RecommendationDao
import com.virex.wallpapers.data.local.SyncedWallpaperDao
import com.virex.wallpapers.data.local.WallpaperDao
import com.virex.wallpapers.data.local.WallpaperDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Hilt Module for Application-level dependencies */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /** Provide Room Database */
    @Provides
    @Singleton
    fun provideWallpaperDatabase(@ApplicationContext context: Context): WallpaperDatabase {
        return Room.databaseBuilder(
                        context,
                        WallpaperDatabase::class.java,
                        WallpaperDatabase.DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
    }

    /** Provide Wallpaper DAO */
    @Provides
    @Singleton
    fun provideWallpaperDao(database: WallpaperDatabase): WallpaperDao {
        return database.wallpaperDao()
    }

    /** Provide Synced Wallpaper DAO */
    @Provides
    @Singleton
    fun provideSyncedWallpaperDao(database: WallpaperDatabase): SyncedWallpaperDao {
        return database.syncedWallpaperDao()
    }

    /** Provide Recommendation DAO */
    @Provides
    @Singleton
    fun provideRecommendationDao(database: WallpaperDatabase): RecommendationDao {
        return database.recommendationDao()
    }

    /** Provide CDN Wallpaper DAO */
    @Provides
    @Singleton
    fun provideCdnWallpaperDao(database: WallpaperDatabase): CdnWallpaperDao {
        return database.cdnWallpaperDao()
    }

    /** Provide Preferences DataStore */
    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): PreferencesDataStore {
        return PreferencesDataStore(context)
    }
}
