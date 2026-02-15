package com.virex.wallpapers.di

import com.virex.wallpapers.data.local.SyncedWallpaperDao
import com.virex.wallpapers.data.remote.api.PexelsApi
import com.virex.wallpapers.data.remote.api.PicsumApi
import com.virex.wallpapers.data.remote.api.UnsplashApi
import com.virex.wallpapers.data.remote.api.VirexBackendApi
import com.virex.wallpapers.data.remote.api.WallhavenApi
import com.virex.wallpapers.data.repository.WallpaperSyncRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

/** Hilt module for sync-related dependencies */
@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    fun provideWallpaperSyncRepository(
            @Named("virex_backend") virexBackendApi: VirexBackendApi,
            unsplashApi: UnsplashApi,
            pexelsApi: PexelsApi,
            wallhavenApi: WallhavenApi,
            picsumApi: PicsumApi,
            syncedWallpaperDao: SyncedWallpaperDao
    ): WallpaperSyncRepository =
            WallpaperSyncRepository(
                    virexBackendApi = virexBackendApi,
                    unsplashApi = unsplashApi,
                    pexelsApi = pexelsApi,
                    wallhavenApi = wallhavenApi,
                    picsumApi = picsumApi,
                    syncedWallpaperDao = syncedWallpaperDao
            )
}
