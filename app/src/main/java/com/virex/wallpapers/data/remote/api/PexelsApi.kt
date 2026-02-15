package com.virex.wallpapers.data.remote.api

import com.virex.wallpapers.data.remote.model.PexelsPhoto
import com.virex.wallpapers.data.remote.model.PexelsSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Pexels API Service
 * 
 * Free tier: 200 requests/hour, 20,000 requests/month
 * No attribution required (but appreciated)
 * All photos are free for personal and commercial use
 * 
 * @see https://www.pexels.com/api/documentation/
 */
interface PexelsApi {
    
    companion object {
        const val BASE_URL = "https://api.pexels.com/v1/"
        const val RATE_LIMIT_HEADER = "X-Ratelimit-Remaining"
    }
    
    /**
     * Get curated photos (hand-picked by Pexels team)
     * 
     * @param apiKey Pexels API key
     * @param page Page number (starts from 1)
     * @param perPage Number of photos per page (max 80, default 15)
     */
    @GET("curated")
    suspend fun getCuratedPhotos(
        @Header("Authorization") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30
    ): Response<PexelsSearchResponse>
    
    /**
     * Search photos by query
     * 
     * @param apiKey Pexels API key
     * @param query Search terms
     * @param page Page number
     * @param perPage Number of results per page (max 80)
     * @param orientation Filter: landscape, portrait, square
     * @param color Filter by color: red, orange, yellow, green, turquoise, blue, violet, pink, brown, black, gray, white
     */
    @GET("search")
    suspend fun searchPhotos(
        @Header("Authorization") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30,
        @Query("orientation") orientation: String? = "portrait",
        @Query("color") color: String? = null
    ): Response<PexelsSearchResponse>
    
    /**
     * Get a single photo by ID
     */
    @GET("photos/{id}")
    suspend fun getPhoto(
        @Header("Authorization") apiKey: String,
        @Path("id") photoId: Int
    ): Response<PexelsPhoto>
}
