package com.virex.wallpapers.data.remote.api

import com.virex.wallpapers.data.remote.model.UnsplashPhoto
import com.virex.wallpapers.data.remote.model.UnsplashSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Unsplash API Service
 * 
 * Free tier: 50 requests/hour for demo apps
 * Production tier: 5,000 requests/hour (requires approval)
 * 
 * @see https://unsplash.com/documentation
 */
interface UnsplashApi {
    
    companion object {
        const val BASE_URL = "https://api.unsplash.com/"
        const val RATE_LIMIT_HEADER = "X-Ratelimit-Remaining"
    }
    
    /**
     * Get curated photos (editorial picks)
     * 
     * @param clientId Unsplash API access key
     * @param page Page number (starts from 1)
     * @param perPage Number of photos per page (max 30)
     * @param orderBy Order by: latest, oldest, popular
     */
    @GET("photos")
    suspend fun getPhotos(
        @Header("Authorization") clientId: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30,
        @Query("order_by") orderBy: String = "latest"
    ): Response<List<UnsplashPhoto>>
    
    /**
     * Search photos by query
     * 
     * @param clientId Unsplash API access key
     * @param query Search terms
     * @param page Page number
     * @param perPage Number of results per page (max 30)
     * @param color Filter by color: black_and_white, black, white, etc.
     * @param orientation Filter by orientation: landscape, portrait, squarish
     */
    @GET("search/photos")
    suspend fun searchPhotos(
        @Header("Authorization") clientId: String,
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30,
        @Query("color") color: String? = null,
        @Query("orientation") orientation: String? = null
    ): Response<UnsplashSearchResponse>
    
    /**
     * Get a single photo by ID
     */
    @GET("photos/{id}")
    suspend fun getPhoto(
        @Header("Authorization") clientId: String,
        @Path("id") photoId: String
    ): Response<UnsplashPhoto>
    
    /**
     * Get random photos
     * 
     * @param query Search query to filter random photos
     * @param count Number of photos to return (max 30)
     */
    @GET("photos/random")
    suspend fun getRandomPhotos(
        @Header("Authorization") clientId: String,
        @Query("query") query: String? = null,
        @Query("count") count: Int = 30,
        @Query("orientation") orientation: String = "portrait"
    ): Response<List<UnsplashPhoto>>
    
    /**
     * Track photo download (REQUIRED by Unsplash API Terms)
     * Call this endpoint when user downloads/sets wallpaper
     */
    @GET("photos/{id}/download")
    suspend fun trackDownload(
        @Header("Authorization") clientId: String,
        @Path("id") photoId: String
    ): Response<UnsplashDownloadResponse>
}

/**
 * Response from download tracking endpoint
 */
data class UnsplashDownloadResponse(
    val url: String
)
