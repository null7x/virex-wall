package com.virex.wallpapers.data.remote.api

import com.virex.wallpapers.data.remote.model.WallhavenSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Wallhaven API Service
 *
 * Free API, no API key required. Works in Russia without VPN.
 *
 * Docs: https://wallhaven.cc/help/api
 */
interface WallhavenApi {

    companion object {
        const val BASE_URL = "https://wallhaven.cc/api/v1/"
    }

    /**
     * Search wallpapers
     *
     * @param query Search query
     * @param categories 1=General, 2=Anime, 3=People (combine: "110" = General+Anime)
     * @param purity 1=SFW, 2=Sketchy, 3=NSFW (combine: "100" = SFW only)
     * @param sorting date_added, relevance, random, views, favorites, toplist
     * @param order desc or asc
     * @param page Page number
     */
    @GET("search")
    suspend fun searchWallpapers(
            @Query("q") query: String = "",
            @Query("categories") categories: String = "100",
            @Query("purity") purity: String = "100",
            @Query("sorting") sorting: String = "toplist",
            @Query("order") order: String = "desc",
            @Query("page") page: Int = 1,
            @Query("ratios") ratios: String = "portrait",
            @Query("atleast") atleast: String = "1080x1920"
    ): WallhavenSearchResponse
}
