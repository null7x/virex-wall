package com.virex.wallpapers.data.remote.api

import com.virex.wallpapers.data.remote.model.PicsumPhoto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Picsum Photos API Service
 *
 * Free API. Last resort fallback. Works globally including Russia.
 *
 * Docs: https://picsum.photos/
 */
interface PicsumApi {

    companion object {
        const val BASE_URL = "https://picsum.photos/"
    }

    /**
     * Get a paginated list of photos
     *
     * @param page Page number
     * @param limit Number of photos per page (max 100)
     */
    @GET("v2/list")
    suspend fun getPhotos(
            @Query("page") page: Int = 1,
            @Query("limit") limit: Int = 30
    ): List<PicsumPhoto>
}
