package com.virex.wallpapers.data.remote.api

import com.virex.wallpapers.data.remote.model.BingImageArchiveResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Bing image archive API.
 *
 * Public endpoint, no API key required.
 */
interface BingApi {

    companion object {
        const val BASE_URL = "https://www.bing.com/"
    }

    @GET("HPImageArchive.aspx")
    suspend fun getImageArchive(
            @Query("format") format: String = "js",
            @Query("idx") idx: Int = 0,
            @Query("n") count: Int = 16,
            @Query("mkt") market: String = "ru-RU"
    ): BingImageArchiveResponse
}
