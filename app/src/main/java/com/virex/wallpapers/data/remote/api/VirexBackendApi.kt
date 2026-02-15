package com.virex.wallpapers.data.remote.api

import com.virex.wallpapers.data.remote.model.VirexWallpaperResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API interface for VIREX Backend (Koyeb)
 * 
 * This is the PRIMARY source for wallpapers in Russia.
 * The backend proxies requests to Pexels, Unsplash, Wallhaven,
 * avoiding direct connections to potentially blocked services.
 */
interface VirexBackendApi {

    companion object {
        // Production URL on Koyeb
        const val BASE_URL = "https://relieved-pigeon-virex-9ada7548.koyeb.app/"
    }

    /**
     * Get trending wallpapers from all sources
     */
    @GET("wallpapers/trending")
    suspend fun getTrending(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30
    ): VirexWallpaperResponse

    /**
     * Search wallpapers by query
     */
    @GET("wallpapers/search")
    suspend fun searchWallpapers(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30
    ): VirexWallpaperResponse

    /**
     * Get wallpapers by category
     */
    @GET("wallpapers/category/{name}")
    suspend fun getByCategory(
        @Path("name") categoryName: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30
    ): VirexWallpaperResponse

    /**
     * Get available categories
     */
    @GET("wallpapers/categories")
    suspend fun getCategories(): VirexCategoriesResponse
}

/**
 * Response model for categories endpoint
 */
data class VirexCategoriesResponse(
    val categories: List<VirexCategoryItem>
)

data class VirexCategoryItem(
    val id: String,
    val name: String,
    val coverUrl: String? = null,
    val count: Int? = 0
)
