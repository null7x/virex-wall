package com.virex.wallpapers.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.virex.wallpapers.data.remote.api.BingApi
import com.virex.wallpapers.data.remote.api.GitHubCdnApi
import com.virex.wallpapers.data.remote.api.PexelsApi
import com.virex.wallpapers.data.remote.api.PicsumApi
import com.virex.wallpapers.data.remote.api.UnsplashApi
import com.virex.wallpapers.data.remote.api.WallhavenApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Hilt module for network dependencies
 *
 * Provides Retrofit instances for:
 * - Unsplash, Pexels (legacy, may be blocked in RU)
 * - GitHub CDN (primary source, works globally)
 * - Wallhaven (backup, works in RU)
 * - Picsum (last fallback)
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val TIMEOUT_SECONDS = 30L
    private const val CDN_TIMEOUT_SECONDS = 15L
    private const val CACHE_SIZE = 10L * 1024 * 1024 // 10 MB

    @Provides @Singleton fun provideGson(): Gson = GsonBuilder().setLenient().create()

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val loggingInterceptor =
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }

        val cacheDir = File(context.cacheDir, "http_cache")
        val cache = Cache(cacheDir, CACHE_SIZE)

        return OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .cache(cache)
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()
    }

    /** OkHttp client with shorter timeouts for CDN/fallback sources */
    @Provides
    @Singleton
    @Named("cdn")
    fun provideCdnOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val loggingInterceptor =
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }

        val cacheDir = File(context.cacheDir, "cdn_cache")
        val cache = Cache(cacheDir, CACHE_SIZE)

        return OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .cache(cache)
                .connectTimeout(CDN_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(CDN_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(CDN_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()
    }

    // ==================== Legacy APIs (may be blocked in RU) ====================

    @Provides
    @Singleton
    @Named("unsplash")
    fun provideUnsplashRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit =
            Retrofit.Builder()
                    .baseUrl(UnsplashApi.BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()

    @Provides
    @Singleton
    @Named("pexels")
    fun providePexelsRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit =
            Retrofit.Builder()
                    .baseUrl(PexelsApi.BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()

    @Provides
    @Singleton
    fun provideUnsplashApi(@Named("unsplash") retrofit: Retrofit): UnsplashApi =
            retrofit.create(UnsplashApi::class.java)

    @Provides
    @Singleton
    fun providePexelsApi(@Named("pexels") retrofit: Retrofit): PexelsApi =
            retrofit.create(PexelsApi::class.java)

    // ==================== CDN APIs (work in Russia) ====================

    @Provides
    @Singleton
    @Named("github_cdn")
    fun provideGitHubCdnRetrofit(@Named("cdn") okHttpClient: OkHttpClient, gson: Gson): Retrofit =
            Retrofit.Builder()
                    .baseUrl(GitHubCdnApi.BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()

    @Provides
    @Singleton
    @Named("wallhaven")
    fun provideWallhavenRetrofit(@Named("cdn") okHttpClient: OkHttpClient, gson: Gson): Retrofit =
            Retrofit.Builder()
                    .baseUrl(WallhavenApi.BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()

    @Provides
    @Singleton
    @Named("picsum")
    fun providePicsumRetrofit(@Named("cdn") okHttpClient: OkHttpClient, gson: Gson): Retrofit =
            Retrofit.Builder()
                    .baseUrl(PicsumApi.BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()

    @Provides
    @Singleton
    @Named("bing")
    fun provideBingRetrofit(@Named("cdn") okHttpClient: OkHttpClient, gson: Gson): Retrofit =
            Retrofit.Builder()
                    .baseUrl(BingApi.BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()

    @Provides
    @Singleton
    fun provideGitHubCdnApi(@Named("github_cdn") retrofit: Retrofit): GitHubCdnApi =
            retrofit.create(GitHubCdnApi::class.java)

    @Provides
    @Singleton
    fun provideWallhavenApi(@Named("wallhaven") retrofit: Retrofit): WallhavenApi =
            retrofit.create(WallhavenApi::class.java)

    @Provides
    @Singleton
    fun providePicsumApi(@Named("picsum") retrofit: Retrofit): PicsumApi =
            retrofit.create(PicsumApi::class.java)

    @Provides
    @Singleton
    fun provideBingApi(@Named("bing") retrofit: Retrofit): BingApi =
            retrofit.create(BingApi::class.java)
}
